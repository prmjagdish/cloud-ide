import React, { useState } from "react";
import CreateProjectMenu from "./CreateProjectMenu";
import JavaProjectModal from "./JavaProjectModal";

const CreateProjectWrapper = () => {

  const [showJavaModal, setShowJavaModal] = useState(false);

  return (
    <>
      <CreateProjectMenu onOpenJavaModal={() => setShowJavaModal(true)} />

      {showJavaModal && <JavaProjectModal onClose={() => setShowJavaModal(false)} />}
    </>
  );
};

export default CreateProjectWrapper;
