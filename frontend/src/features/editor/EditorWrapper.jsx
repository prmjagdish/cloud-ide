import { React, useState } from "react";
import CodeEditor from "@/features/editor/CodeEditor";

const EditorArea = () => {
  const [code, setCode] = useState("// Start coding...");

  return (
    <CodeEditor
      language="javascript"
      theme="vs-dark"
      value={code}
      onChange={(val) => setCode(val || "")}
    />
  );
};

export default EditorArea;
