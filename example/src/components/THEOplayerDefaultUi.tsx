import React, { ReactNode, useState } from 'react';
import { PlayerConfiguration, THEOplayer, THEOplayerView } from 'react-native-theoplayer';
import { SeekBar } from './seekbar/SeekBar';
import { AirplayButton } from './button/AirplayButton';
import { ChromecastButton, ENABLE_CAST_BUTTON } from './button/ChromecastButton';
import { ControlBar } from './controlbar/ControlBar';
import { TimeLabel } from './timelabel/TimeLabel';
import { FullscreenButton } from './button/FullscreenButton';
import { LanguageMenuButton } from './menu/LanguageMenuButton';
import { SettingsMenuButton } from './menu/SettingsMenuButton';
import { MuteButton } from './button/MuteButton';
import { CastMessage } from './message/CastMessage';
import { CenteredDelayedActivityIndicator } from './activityindicator/CenteredDelayedActivityIndicator';
import { CENTER_BUTTON_SIZE, defaultPlayerStyle, THEOplayerStyle } from './THEOplayerStyle';
import { View } from 'react-native';
import { UiContainer } from './uicontroller/UiContainer';
import { PlayButton } from './button/PlayButton';
import { QualitySubMenu } from './menu/QualitySubMenu';
import { PlaybackRateSubMenu } from './menu/PlaybackRateSubMenu';
import { SkipButton } from './button/SkipButton';
import { Spacer } from './controlbar/Spacer';

export interface THEOplayerDefaultUiProps {
  style?: Partial<THEOplayerStyle>;
  config?: PlayerConfiguration;
  onPlayerReady?: (player: THEOplayer) => void;
  topSlot?: ReactNode;
  bottomSlot?: ReactNode;
}

export function THEOplayerDefaultUi(props: THEOplayerDefaultUiProps) {
  const { style, config, topSlot, bottomSlot } = props;
  const [player, setPlayer] = useState<THEOplayer | undefined>(undefined);
  const chromeless = config?.chromeless ?? false;

  const onPlayerReady = (player: THEOplayer) => {
    setPlayer(player);
    props.onPlayerReady?.(player);
  };

  return (
    <THEOplayerView config={config} onPlayerReady={onPlayerReady}>
      {player !== undefined && chromeless && (
        <UiContainer
          style={{ ...defaultPlayerStyle, ...style }}
          player={player}
          top={
            <ControlBar>
              {topSlot}
              {ENABLE_CAST_BUTTON && (
                <>
                  <AirplayButton />
                  <ChromecastButton />
                </>
              )}
              <LanguageMenuButton />
              <SettingsMenuButton>
                {/*Note: quality selection is not available on iOS */}
                <QualitySubMenu />
                <PlaybackRateSubMenu />
              </SettingsMenuButton>
            </ControlBar>
          }
          center={
            <ControlBar style={{ height: CENTER_BUTTON_SIZE }}>
              <Spacer grow={3} />
              <SkipButton skip={-10} style={{ height: CENTER_BUTTON_SIZE, width: CENTER_BUTTON_SIZE }} />
              <Spacer />
              <PlayButton style={{ height: CENTER_BUTTON_SIZE, width: CENTER_BUTTON_SIZE }} />
              <Spacer />
              <SkipButton skip={30} style={{ height: CENTER_BUTTON_SIZE, width: CENTER_BUTTON_SIZE }} />
              <Spacer grow={3} />
              <CenteredDelayedActivityIndicator />
            </ControlBar>
          }
          bottom={
            <>
              <ControlBar>
                <CastMessage />
                <View style={{ flexGrow: 1 }} />
              </ControlBar>
              <ControlBar>
                <SeekBar />
              </ControlBar>

              <ControlBar>
                <MuteButton />
                <TimeLabel showDuration={true} />

                <Spacer />

                {bottomSlot}
                <FullscreenButton />
              </ControlBar>
            </>
          }></UiContainer>
      )}
    </THEOplayerView>
  );
}
