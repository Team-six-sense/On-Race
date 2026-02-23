import {
  Modal,
  ModalClose,
  ModalContent,
  ModalDescription,
  ModalFooter,
  ModalHeader,
  ModalTitle,
  ModalTrigger,
} from '@/components/ui/modal';
import { Button } from '@/components/ui/button';
import { useState } from 'react';

export default function ModalDemo() {
  const [open, setOpen] = useState(false);
  const handleEvent = () => {
    setOpen(false);
  };
  return (
    <Modal open={open} onOpenChange={setOpen}>
      <ModalTrigger asChild>
        <Button variant="outline">Modal Open</Button>
      </ModalTrigger>
      {/* cva를 통해 정의한 size="lg" 적용 */}
      <ModalContent size="lg">
        <ModalHeader>
          <ModalTitle className="text-full">Title</ModalTitle>
          <ModalDescription>Desctiption</ModalDescription>
        </ModalHeader>
        {/* <div className="py-4">
          <p>여기에 모달 본문 내용을 넣습니다.</p>
        </div> */}
        <ModalFooter>
          <ModalClose asChild>
            <Button variant="secondary" rounded="full">
              Button Text
            </Button>
          </ModalClose>

          <Button variant="primary1" rounded="full" onClick={handleEvent}>
            Button Text
          </Button>
        </ModalFooter>
      </ModalContent>
    </Modal>
  );
}
