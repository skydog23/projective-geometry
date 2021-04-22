package charlesgunn.jreality.newtools;

import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;

public class MyTool extends AbstractTool {
	SceneGraphPath attachmentPath;

	public MyTool(InputSlot activationSlot) {
		super(activationSlot);
	}

	public MyTool() {
	}

	public SceneGraphPath getAttachmentPath() {
		return attachmentPath;
	}

	public void setAttachmentPath(SceneGraphPath attachmentPath) {
		this.attachmentPath = attachmentPath;
	}
}
