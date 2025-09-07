import React from "react";
import { Button } from "@/components/ui/button";
import { Play, Bug, MoreVertical } from "lucide-react";

const RunControls = () => {
  return (
    <div className="flex items-center gap-3">
      <select className="bg-[#252526] text-white px-2 py-1 rounded">
        <option>Select file to run</option>
        <option>index.js</option>
        <option>app.java</option>
        <option>main.py</option>
      </select>
      <Button variant="default" className="bg-[#252526] hover:bg-green-700">
        <Play size={18} className="mr-1" /> Run
      </Button>
      <Button variant="default" className="bg-[#252526] hover:bg-yellow-700">
        <Bug size={18} className="mr-1" /> Debug
      </Button>
      <Button variant="ghost">
        <MoreVertical />
      </Button>
    </div>
  );
};

export default RunControls;
