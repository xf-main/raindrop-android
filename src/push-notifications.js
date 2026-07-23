import { useEffect } from 'react'
import { useSelector } from 'react-redux'
import { user } from 'data/selectors/user'
import * as fcm from 'modules/fcm'
import Api from 'data/modules/api'

export default function PushNotifications({ children }) {
    const { _id } = useSelector(user)

    useEffect(()=>{
        if (!_id) return

        fcm.getToken()
            .then(token=>
                Api._post('user/connect/fcm_device', { token })
            )
            //best effort: FIS_AUTH_ERROR etc. on emulators/devices without valid play services
            .catch(e=>
                console.warn('push notifications registration failed:', e?.message)
            )
    }, [_id])

    return children
}