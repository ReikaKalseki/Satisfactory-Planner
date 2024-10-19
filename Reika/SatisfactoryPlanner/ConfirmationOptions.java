package Reika.SatisfactoryPlanner;


public enum ConfirmationOptions {

	NEWOPEN(true, "Switching factory files with unsaved changes", "You have unsaved changes, which will be lost. Are you sure you want to load a new file?"),
	CLOSE(true, "Closing the application with unsaved changes", "You have unsaved changes, which will be lost. Are you sure you want to close the program?"),

	SAVEDIFFNAME(true, "Saving a factory to the original file after changing its name", "Factory name changed but you are saving to the original file (%s). Do you really want to overwrite?"),

	RELOAD(true, "Reloading a factory from disk", "Are you sure you want to do this, losing all changes?"),
	CLEARCRAFT(true, "Clearing factory recipe data", "Are you sure?"),
	ZEROCRAFT(false, "Zeroing factory crafting counts", "Are you sure?"),
	ISOLATE(true, "Removing factory external supplies", "Are you sure?"),
	CLEARPROD(false, "Removing factory desired products", "Are you sure?"),
	CLEANUP(false, "Running factory cleanup", "Are you sure?"),

	;

	private boolean enabled;
	public final String displayName;
	private final String message;

	private ConfirmationOptions(boolean def, String disp, String msg) {
		enabled = def;
		displayName = disp;
		message = msg;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setState(boolean en) {
		enabled = en;
	}

	public String getMessage(Object... args) {
		return String.format(message, args);
	}

	public String getSettingKey() {
		return "CONFIRM_"+this.name();
	}

}
