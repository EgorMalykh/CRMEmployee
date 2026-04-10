import {React, useRef, useEffect} from 'react';
import { createPortal } from 'react-dom'

const ModalWindow = ({children, open}) => {
    const dialog = useRef();

    useEffect(() => {
        if (open) {
            dialog.current.showModal();
        } else {
            dialog.current.close();
        }
    }, [open]);



    return createPortal(
        <dialog ref={dialog}>
            <div className="fixed inset-0 grid place-content-center bg-black/50 transition-all">
                 <div className="w-full scale-75 bg-white p-4 rounded-md">
                     {children}
                 </div>
           </div>
        </dialog>,
        document.getElementById('modal')
    )
}

export default ModalWindow;