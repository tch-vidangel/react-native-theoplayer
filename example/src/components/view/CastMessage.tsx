import React, { PureComponent } from 'react';
import { CastEvent, CastEventType, PlayerEventType, THEOplayerInternal } from 'react-native-theoplayer';
import { Text } from 'react-native';
import { PlayerContext } from '../util/Context';
import { PlayerStyleContext, VideoPlayerStyle } from '../style/VideoPlayerStyle';

interface CastMessageState {
  message: string | undefined;
}

export class CastMessage extends PureComponent<unknown, CastMessageState> {
  constructor(props: unknown) {
    super(props);
    this.state = { message: undefined };
  }

  componentDidMount() {
    const player = this.context as THEOplayerInternal;
    player.addEventListener(PlayerEventType.CAST_EVENT, this.onCastEvent);
  }

  componentWillUnmount() {
    const player = this.context as THEOplayerInternal;
    player.removeEventListener(PlayerEventType.CAST_EVENT, this.onCastEvent);
  }

  private onCastEvent = (event: CastEvent) => {
    if (event.subType !== CastEventType.CHROMECAST_STATE_CHANGE && event.subType !== CastEventType.AIRPLAY_STATE_CHANGE) {
      return;
    }
    const stateEvent = event;
    let message = undefined;
    const castTarget = event.subType === CastEventType.CHROMECAST_STATE_CHANGE ? 'chromecast' : 'airplay';
    switch (stateEvent.state) {
      case 'connecting':
        message = `Connecting to ${castTarget} ...`;
        break;
      case 'connected':
        message = `Playing on ${castTarget}`;
        break;
    }
    this.setState({
      message,
    });
  };

  render() {
    const { message } = this.state;

    if (!message) {
      return <></>;
    }

    return (
      <PlayerStyleContext.Consumer>
        {(styleContext: VideoPlayerStyle) => <Text style={styleContext.videoPlayer.message}>{message}</Text>}
      </PlayerStyleContext.Consumer>
    );
  }
}

CastMessage.contextType = PlayerContext;
