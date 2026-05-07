import React from 'react';
import { Modal, Pressable, Text, View } from 'react-native';
import { Btn } from './Buttons';

type Props = {
  visible: boolean;
  action: string;
  onCancel: () => void;
  onApprove: () => void;
};

// Mocked Mobile Wallet Adapter approval sheet, styled like Phantom's. Wire to
// @solana-mobile/mobile-wallet-adapter-protocol when integrating real signing.
export const MWASheet = ({ visible, action, onCancel, onApprove }: Props) => (
  <Modal visible={visible} transparent animationType="slide" onRequestClose={onCancel}>
    <Pressable
      onPress={onCancel}
      style={{ flex: 1, backgroundColor: 'rgba(0,0,0,0.7)', justifyContent: 'flex-end' }}
    >
      <Pressable
        onPress={(e) => e.stopPropagation()}
        style={{
          backgroundColor: '#1d1b20',
          borderTopLeftRadius: 16,
          borderTopRightRadius: 16,
          paddingHorizontal: 20,
          paddingTop: 20,
          paddingBottom: 28,
        }}
      >
        <View style={{ alignItems: 'center', marginBottom: 12 }}>
          <View
            style={{ width: 32, height: 4, backgroundColor: '#49454f', borderRadius: 2 }}
          />
        </View>

        <View
          style={{ flexDirection: 'row', alignItems: 'center', gap: 10, marginBottom: 16 }}
        >
          <View
            style={{
              width: 36,
              height: 36,
              borderRadius: 8,
              backgroundColor: '#5e4fff',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <Text style={{ color: '#fff', fontWeight: '700', fontSize: 18 }}>P</Text>
          </View>
          <View>
            <Text style={{ fontSize: 15, color: '#e6e1e5' }}>Phantom</Text>
            <Text style={{ fontSize: 12, color: '#9a8e72' }}>Mobile Wallet Adapter</Text>
          </View>
        </View>

        <Text style={{ fontSize: 13, color: '#cac4d0', marginBottom: 16, lineHeight: 20 }}>
          Clan World requests your signature to:
        </Text>

        <View
          style={{
            backgroundColor: '#2b2930',
            borderRadius: 8,
            padding: 12,
            marginBottom: 16,
          }}
        >
          <Text style={{ fontSize: 13, color: '#e6e1e5' }}>{action}</Text>
        </View>

        <View style={{ flexDirection: 'row', gap: 8 }}>
          <Btn variant="secondary" onPress={onCancel} style={{ flex: 1, borderRadius: 100 }}>
            Cancel
          </Btn>
          <Pressable
            onPress={onApprove}
            style={({ pressed }) => ({
              flex: 1.4,
              backgroundColor: '#ab9ff2',
              borderRadius: 100,
              paddingVertical: 12,
              paddingHorizontal: 16,
              alignItems: 'center',
              opacity: pressed ? 0.85 : 1,
            })}
          >
            <Text style={{ color: '#1d1b20', fontWeight: '500', fontSize: 13 }}>Approve</Text>
          </Pressable>
        </View>
      </Pressable>
    </Pressable>
  </Modal>
);
