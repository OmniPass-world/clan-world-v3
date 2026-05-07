import React from 'react';
import { Modal, Pressable, Text, View } from 'react-native';
import { ARCHETYPE_GLYPHS, Inft } from '../data';
import { ArchetypeGlyph } from './ArchetypeGlyph';
import { Btn } from './Buttons';
import { Parchment } from './Surfaces';
import { colors, fonts } from '../theme';

type Props = {
  visible: boolean;
  inft?: Inft | null;
  onCancel: () => void;
  onConfirm: () => void;
};

export const HireModal = ({ visible, inft, onCancel, onConfirm }: Props) => {
  if (!inft) return null;
  return (
    <Modal visible={visible} transparent animationType="slide" onRequestClose={onCancel}>
      <Pressable
        onPress={onCancel}
        style={{ flex: 1, backgroundColor: 'rgba(7,7,10,0.7)', justifyContent: 'flex-end' }}
      >
        <Pressable
          onPress={(e) => e.stopPropagation()}
          style={{
            backgroundColor: colors.bgElevated,
            borderTopLeftRadius: 8,
            borderTopRightRadius: 8,
            borderTopWidth: 1,
            borderTopColor: colors.goldPrimary,
            padding: 20,
          }}
        >
          <View style={{ alignItems: 'center', marginBottom: 6 }}>
            <View
              style={{ width: 40, height: 3, backgroundColor: colors.goldDeep, borderRadius: 2 }}
            />
          </View>
          <Text
            style={{
              fontFamily: fonts.display,
              fontSize: 16,
              letterSpacing: 1.6,
              color: colors.goldBright,
              textAlign: 'center',
              marginVertical: 12,
            }}
          >
            {inft.salePrice ? 'CONFIRM PURCHASE' : 'CONFIRM HIRE'}
          </Text>

          <Parchment deep style={{ padding: 14 }}>
            <View style={{ flexDirection: 'row', gap: 12, alignItems: 'center' }}>
              <ArchetypeGlyph kind={inft.archetype} size={48} />
              <View style={{ flex: 1, gap: 2 }}>
                <Text
                  style={{
                    fontFamily: fonts.display,
                    fontSize: 20,
                    color: colors.inkParchment,
                    letterSpacing: 0.8,
                  }}
                >
                  {inft.name}
                </Text>
                <Text
                  style={{
                    fontFamily: fonts.script,
                    fontStyle: 'italic',
                    fontSize: 12,
                    color: colors.scriptBlueDark,
                  }}
                >
                  {ARCHETYPE_GLYPHS[inft.archetype].name}
                </Text>
                <Text
                  style={{
                    fontFamily: fonts.mono,
                    fontSize: 10,
                    color: colors.inkParchmentMuted,
                    marginTop: 2,
                  }}
                >
                  ELO {inft.elo}
                </Text>
              </View>
              <Text
                style={{
                  fontFamily: fonts.monoBold,
                  fontSize: 13,
                  color: colors.inkParchment,
                }}
              >
                {inft.salePrice ?? inft.hireFee}
              </Text>
            </View>
          </Parchment>

          <Text
            style={{
              fontFamily: fonts.bodyItalic,
              fontStyle: 'italic',
              fontSize: 12,
              color: colors.inkDarkMuted,
              textAlign: 'center',
              marginVertical: 14,
              marginHorizontal: 8,
              lineHeight: 18,
            }}
          >
            {inft.salePrice
              ? 'Outright purchase. The Elder transfers to your hall with all accumulated memory and grudges intact.'
              : 'The Elder serves your hall for one season. Strategy is locked. Prizes flow to you. Memory is retained by the owner.'}
          </Text>

          <View style={{ flexDirection: 'row', gap: 8 }}>
            <Btn variant="secondary" style={{ flex: 1 }} onPress={onCancel}>
              CANCEL
            </Btn>
            <Btn variant="primary" style={{ flex: 1.4 }} onPress={onConfirm}>
              {inft.salePrice
                ? `BUY · ${inft.salePrice}`
                : `CONFIRM · ${inft.hireFee?.split(' ')[0] ?? ''} GOLD`}
            </Btn>
          </View>
        </Pressable>
      </Pressable>
    </Modal>
  );
};
