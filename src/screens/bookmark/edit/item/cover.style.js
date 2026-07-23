import styled from 'styled-components/native'
import { Touchable } from 'react-native-gesture-handler'

export const Tap = styled(Touchable).attrs({ activeOpacity: 0.2 })`
	align-items: flex-start;
	justify-content: flex-start;
`