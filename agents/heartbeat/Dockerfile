# syntax=docker/dockerfile:1.7
#
# clanworld/heartbeat:dev — heartbeat container
#
# Phase 1.10 of docs/plans/dockerize-elder-infra-v1.md
# Implements: GitHub issue clan-world/clan-world-game#353
#
# This container ticks the diamond's heartbeat() every N seconds and posts an
# HMAC-SHA256-signed webhook to the self-hosted Convex backend after each
# successful on-chain tick.
#
# Design: thin bash + foundry cast + openssl + curl + jq, under tini for PID 1
# signal forwarding. NO node runtime — keeps the image small and the failure
# surface to a single shell loop.

FROM debian:bookworm-slim

# System deps — bash + curl for webhook POST, jq for cast --json parsing,
# openssl for HMAC-SHA256, tini for PID 1, ca-certificates for HTTPS RPC,
# dnsutils for preflight diagnostics. git is required by `foundryup`.
RUN apt-get update && apt-get install -y --no-install-recommends \
      bash \
      ca-certificates \
      curl \
      git \
      jq \
      openssl \
      tini \
      dnsutils \
    && rm -rf /var/lib/apt/lists/*

# Foundry cast — pinned via foundryup. Move to /usr/local/bin so it is on
# PATH for the non-root runtime user without an extra PATH dance.
RUN curl -L https://foundry.paradigm.xyz | bash \
    && /root/.foundry/bin/foundryup \
    && mv /root/.foundry/bin/cast /usr/local/bin/cast \
    && rm -rf /root/.foundry

# Non-root runtime user. Heartbeat container does not need root once cast +
# system binaries are in place.
ARG HEARTBEAT_UID=1000
ARG HEARTBEAT_GID=1000
RUN groupadd -g ${HEARTBEAT_GID} heartbeat \
    && useradd -m -u ${HEARTBEAT_UID} -g ${HEARTBEAT_GID} -s /bin/bash heartbeat

# Scripts. entrypoint.sh sources preflight.sh and webhook-sign.sh, so all
# three live in /opt/clan-world/heartbeat with executable bits.
COPY --chmod=755 entrypoint.sh    /opt/clan-world/heartbeat/entrypoint.sh
COPY --chmod=755 preflight.sh     /opt/clan-world/heartbeat/preflight.sh
COPY --chmod=755 webhook-sign.sh  /opt/clan-world/heartbeat/webhook-sign.sh

USER heartbeat
WORKDIR /home/heartbeat

# Touched on every successful tick — referenced by the compose healthcheck
# (test -f /tmp/last-heartbeat-success && age < 120s).
ENV HEARTBEAT_TIMESTAMP_FILE=/tmp/last-heartbeat-success

# Default loop interval — overridable per environment.
ENV HEARTBEAT_INTERVAL_SECONDS=60

# tini reaps zombies and forwards signals so `docker stop` does not orphan
# a running cast process mid-tick.
ENTRYPOINT ["/usr/bin/tini", "--", "/opt/clan-world/heartbeat/entrypoint.sh"]
