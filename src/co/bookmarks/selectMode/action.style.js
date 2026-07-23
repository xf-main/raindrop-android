import styled from 'styled-components/native'
import _ from 'lodash-es'
import { Touchable } from 'react-native-gesture-handler'

export const Container = styled.View`
    flex: 1;
`

export const Button = styled(Touchable).attrs({ activeOpacity: 0.2 })`
    flex: 1;
    align-items: center;
    justify-content: center;
    height: 64px;
`

export const Label = styled.Text.attrs({
    numberOfLines: 1
})`
    color: ${({color, theme})=>{
        const found = _.get(theme, color)
        return typeof found == 'string' ? found : ''
    }};
    font-size: ${({theme})=>theme.fontSize.quaternary}px;
    margin-top: 2px;
    padding: 0 8px;
`