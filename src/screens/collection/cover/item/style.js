import styled from 'styled-components/native'
import { Touchable } from 'react-native-gesture-handler'
import { itemHeight, iconHeight } from '../config'

export const Container = styled.View`
    flex: 0.2;
`

export const Press = styled(Touchable).attrs({ androidRipple: {} })`
    flex: 1;
    align-items: center;
    justify-content: center;
    height: ${itemHeight}px;
`

//image
const defaultSource = {
	light: require('./defaultSource.light.png'),
	dark: require('./defaultSource.dark.png')
}

export const Image = styled.Image.attrs(({theme})=>({
    // defaultSource: defaultSource[theme.dark ? 'dark' : 'light'],
    resizeMethod: 'resize'
}))`
    width: ${iconHeight}px;
    height: ${iconHeight}px;
    border-radius: 3px;
`