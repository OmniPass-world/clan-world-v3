//! SECURITY-DEMO-POSTURE: ClanWorld v1 demo intentionally ships an uncapped GOLD
//! faucet. Any signer may call `claim()` repeatedly and mint 100,000 GOLD per call.
//! Combined with the permissive whisper/doctrine writeback path in
//! `apps/server/convex/gold.ts`, this means "GOLD is effectively free" is the
//! accepted v1 posture. Per-recipient cooldowns + EVM↔Solana wallet binding move
//! to v2.

use anchor_lang::prelude::*;
use anchor_spl::token::{self, Mint, MintTo, Token, TokenAccount};

declare_id!("aMwXvN227YK14C8eFQzgyk9h7TfkMk1XQkekZXS6WWQ");

pub const FAUCET_AMOUNT: u64 = 100_000;

#[program]
pub mod gold_faucet {
    use super::*;

    pub fn claim(ctx: Context<Claim>) -> Result<()> {
        let seeds: &[&[u8]] = &[
            b"mint-authority",
            &[ctx.bumps.mint_authority],
        ];
        let signer = &[seeds];
        token::mint_to(
            CpiContext::new_with_signer(
                ctx.accounts.token_program.to_account_info(),
                MintTo {
                    mint: ctx.accounts.gold_mint.to_account_info(),
                    to: ctx.accounts.recipient_ata.to_account_info(),
                    authority: ctx.accounts.mint_authority.to_account_info(),
                },
                signer,
            ),
            FAUCET_AMOUNT,
        )
    }
}

#[derive(Accounts)]
pub struct Claim<'info> {
    #[account(mut)]
    pub payer: Signer<'info>,
    #[account(mut)]
    pub gold_mint: Account<'info, Mint>,
    #[account(
        mut,
        constraint = recipient_ata.mint == gold_mint.key() @ GoldFaucetError::InvalidRecipient,
        constraint = recipient_ata.owner == payer.key() @ GoldFaucetError::InvalidRecipient
    )]
    pub recipient_ata: Account<'info, TokenAccount>,
    /// CHECK: PDA signs mint_to.
    #[account(seeds = [b"mint-authority"], bump)]
    pub mint_authority: UncheckedAccount<'info>,
    pub token_program: Program<'info, Token>,
}

#[error_code]
pub enum GoldFaucetError {
    #[msg("The recipient token account does not belong to the faucet caller.")]
    InvalidRecipient,
}
