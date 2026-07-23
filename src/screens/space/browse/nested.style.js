import styled from 'styled-components/native'
import { Touchable, LegacyScrollView as ScrollView } from 'react-native-gesture-handler'

export const Strip = styled(ScrollView).attrs({
    horizontal: true,
    showsHorizontalScrollIndicator: false,
    fadingEdgeLength: { end: 64 }
})`${({ theme })=>`
    margin-vertical: ${theme.padding.small}px;
`}`

export const Wrap = styled.View`${({ theme })=>`
    flex-direction: row;
    padding-left: ${theme.padding.medium}px;
    padding-right: ${theme.padding.large}px;
    gap: ${theme.padding.small+theme.padding.micro}px;
`}`

export const CollectionTap = styled(Touchable).attrs({ activeOpacity: 0.2 })`${({ theme })=>`
    border-radius: ${theme.padding.medium}px;
`}`

export const CollectionContent = styled.View.attrs({

})`${({ theme })=>`
    flex-direction: row;
    align-items: center;
    justify-content: center;
    padding: 0 ${theme.padding.small+theme.padding.micro}px;
    height: ${theme.height.button}px;
    border-radius: ${theme.padding.medium}px;
    background: ${theme.background.alternative};
    gap: ${theme.padding.small+theme.padding.micro}px;
`}`

export const CollectionText = styled.Text`${({ theme })=>`
    color: ${theme.text.regular};
    font-size: ${theme.fontSize.secondary}px;
`}`