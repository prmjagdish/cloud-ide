import React from "react";
import { Button } from "@/components/ui/button";
import { Play, StopCircle, MoreVertical } from "lucide-react";

const RunControls = () => {
  return (
    <div className="flex items-center gap-3 ml-20">   
      <Button variant="default" className="bg-[#252526] hover:bg-green-700">
        <Play size={18} className="mr-1" /> Run
      </Button>
      <Button variant="default" className="bg-[#252526] hover:bg-red-500">
        <StopCircle size={18} className="mr-1" /> Stop
      </Button>
      <Button variant="ghost">
        <MoreVertical />
      </Button>
    </div>
  );
};

export default RunControls;
