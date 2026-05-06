import React, { useEffect, useRef, useState } from 'react';
import {
  KeyboardAvoidingView,
  Platform,
  Pressable,
  ScrollView,
  Text,
  TextInput,
  View,
} from 'react-native';
import { CHAT_LOG, ChatMessage, Inft } from '../data';
import { Btn, Chip } from '../components/Buttons';
import { Parchment, Stone } from '../components/Surfaces';
import { ResStrip } from '../components/Resources';
import { TopBar } from '../components/TopBar';
import { colors, fonts } from '../theme';

const SUGGESTIONS = [
  'Pace yourself',
  'Push aggressive',
  'Defend the wall',
  'Watch the bandits',
  'Trade for wood',
];

type Props = {
  inft: Inft;
  onBack: () => void;
  onSend?: () => void;
};

export const SteeringConsoleScreen = ({ inft, onBack, onSend }: Props) => {
  const [draft, setDraft] = useState('');
  const [log, setLog] = useState<ChatMessage[]>(CHAT_LOG);
  const scrollRef = useRef<ScrollView>(null);

  useEffect(() => {
    scrollRef.current?.scrollToEnd({ animated: true });
  }, [log.length]);

  const send = () => {
    if (!draft.trim()) return;
    const t = `T${260 + log.filter((l) => l.kind === 'human').length}`;
    setLog((l) => [...l, { kind: 'human', t, text: draft, pending: true }]);
    setDraft('');
    setTimeout(() => {
      setLog((l) => [
        ...l,
        { kind: 'orch', t, text: `▸ Whisper acknowledged · injected at ${t}` },
      ]);
    }, 900);
    onSend?.();
  };

  return (
    <KeyboardAvoidingView
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
      style={{ flex: 1 }}
    >
      <TopBar
        title={`WHISPER · ${inft.name.toUpperCase()}`}
        onBack={onBack}
        right={
          <Text style={{ fontFamily: fonts.mono, fontSize: 10, color: colors.statusLive }}>
            ● TICK {inft.gameTick}
          </Text>
        }
      />

      {/* Context strip */}
      {inft.resources && (
        <View
          style={{
            paddingHorizontal: 14,
            paddingVertical: 8,
            backgroundColor: colors.bgElevated,
            borderBottomWidth: 1,
            borderBottomColor: 'rgba(212,175,92,0.15)',
          }}
        >
          <ResStrip values={inft.resources} compact dark />
        </View>
      )}

      <ScrollView
        ref={scrollRef}
        contentContainerStyle={{ padding: 14, gap: 10 }}
        showsVerticalScrollIndicator={false}
      >
        {log.map((m, i) => (
          <ChatBubble key={i} msg={m} />
        ))}
      </ScrollView>

      {/* Suggestions */}
      <View
        style={{
          paddingHorizontal: 14,
          paddingVertical: 6,
          borderTopWidth: 1,
          borderTopColor: 'rgba(212,175,92,0.1)',
        }}
      >
        <ScrollView
          horizontal
          showsHorizontalScrollIndicator={false}
          contentContainerStyle={{ gap: 6, paddingBottom: 4 }}
        >
          {SUGGESTIONS.map((s) => (
            <Chip key={s} onPress={() => setDraft((d) => (d ? `${d} ${s}` : s))}>
              {s}
            </Chip>
          ))}
        </ScrollView>
      </View>

      {/* Input */}
      <View
        style={{
          flexDirection: 'row',
          padding: 10,
          gap: 8,
          backgroundColor: colors.bgCanvas,
          borderTopWidth: 1,
          borderTopColor: 'rgba(212,175,92,0.3)',
        }}
      >
        <TextInput
          value={draft}
          onChangeText={setDraft}
          placeholder="Whisper into the next tick…"
          placeholderTextColor={colors.inkDarkFaint}
          onSubmitEditing={send}
          style={{
            flex: 1,
            backgroundColor: colors.bgElevated,
            borderColor: colors.goldDeep,
            borderWidth: 1,
            color: colors.inkDark,
            paddingHorizontal: 12,
            paddingVertical: 10,
            fontFamily: fonts.body,
            fontSize: 13,
          }}
        />
        <Btn variant="primary" onPress={send} disabled={!draft.trim()} style={{ minWidth: 90 }}>
          WHISPER
        </Btn>
      </View>
    </KeyboardAvoidingView>
  );
};

const ChatBubble = ({ msg }: { msg: ChatMessage }) => {
  if (msg.kind === 'human') {
    return (
      <View style={{ alignSelf: 'flex-end', maxWidth: '82%' }}>
        <Parchment style={{ paddingHorizontal: 12, paddingVertical: 8 }}>
          <Text style={{ fontFamily: fonts.body, fontSize: 13, color: colors.inkParchment }}>
            {msg.text}
          </Text>
        </Parchment>
        <Text
          style={{
            fontFamily: fonts.mono,
            fontSize: 9,
            color: colors.inkDarkFaint,
            marginTop: 4,
            textAlign: 'right',
          }}
        >
          {msg.t}
          {msg.pending ? ' · sending…' : ''}
        </Text>
      </View>
    );
  }
  if (msg.kind === 'orch') {
    return (
      <View style={{ alignSelf: 'center', maxWidth: '90%' }}>
        <Text
          style={{
            fontFamily: fonts.bodyItalic,
            fontStyle: 'italic',
            fontSize: 12,
            color: colors.inkDarkMuted,
            textAlign: 'center',
          }}
        >
          {msg.text}
        </Text>
        <Text
          style={{
            fontFamily: fonts.mono,
            fontSize: 9,
            color: colors.inkDarkFaint,
            marginTop: 2,
            textAlign: 'center',
          }}
        >
          {msg.t}
        </Text>
      </View>
    );
  }
  return (
    <View style={{ alignSelf: 'flex-start', maxWidth: '82%' }}>
      <View
        style={{
          flexDirection: 'row',
          alignItems: 'center',
          gap: 6,
          marginBottom: 4,
        }}
      >
        <View
          style={{
            backgroundColor: 'rgba(212,175,92,0.12)',
            paddingHorizontal: 5,
            paddingVertical: 2,
          }}
        >
          <Text
            style={{
              fontFamily: fonts.mono,
              fontSize: 8,
              color: colors.goldPrimary,
              letterSpacing: 1,
            }}
          >
            ↗ {msg.target}
          </Text>
        </View>
      </View>
      <Stone style={{ paddingHorizontal: 12, paddingVertical: 8 }}>
        <Text
          style={{
            fontFamily: fonts.bodyItalic,
            fontStyle: 'italic',
            fontSize: 13,
            color: colors.inkDark,
          }}
        >
          "{msg.text}"
        </Text>
      </Stone>
      <Text
        style={{
          fontFamily: fonts.mono,
          fontSize: 9,
          color: colors.inkDarkFaint,
          marginTop: 4,
        }}
      >
        {msg.t}
      </Text>
    </View>
  );
};
