import React from "react";
import { Editor } from "@monaco-editor/react";

function CodeEditor({
  language = "javascript",
  theme = "vs-dark",
  value = "",
  onChange,
}) {
  return (
    <div className="h-full w-full">
      <Editor
        height="100%"
        defaultLanguage={language}
        defaultValue={value}
        theme={theme}
        onChange={onChange}
        options={{
          minimap: { enabled: false },
          fontSize: 14,
          scrollBeyondLastLine: false,
          automaticLayout: true,
        }}
      />
    </div>
  );
}

export default CodeEditor;
