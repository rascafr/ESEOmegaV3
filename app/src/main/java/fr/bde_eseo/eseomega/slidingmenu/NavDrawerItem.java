package fr.bde_eseo.eseomega.slidingmenu;

// Model for our sidelist
public class NavDrawerItem {

    private String title;
    private String id;
	private String moreData;

    // TODO in a int description type
    private boolean isProfile;
	private boolean isDivider;
    private boolean isOption;

	private int icon;
	private String count = "0";
	// boolean to set visiblity of the counter
	private boolean isCounterVisible = false;

    /**
     * Item
     */
	public NavDrawerItem(String title, int icon){
		this.title = title;
		this.icon = icon;
        isProfile = false;
        isDivider = false;
        isOption = false;
	}

    /**
     * Profile
     */
    public NavDrawerItem(String title, String id){
        this.title = title;
        this.id = id;
        isProfile = true;
        isDivider = false;
        isOption = false;
    }

    /**
     * Divider
     */
    public NavDrawerItem() {
        isProfile = false;
        isDivider = true;
        isOption = false;
    }

    public NavDrawerItem(String title) {
        this.title = title;
        isProfile = false;
        isDivider = false;
        isOption = true;
    }

    /**
     * All / not used now
     */
	public NavDrawerItem(String title, int icon, boolean isCounterVisible, String count){
		this.title = title;
		this.icon = icon;
		this.isCounterVisible = isCounterVisible;
		this.count = count;
	}

	public String getMoreData() {
		return moreData;
	}

	public void setMoreData(String moreData) {
		this.moreData = moreData;
	}

	public boolean isEmpty() {
		return title.length() == 0;
	}
	
	public String getTitle(){
		return this.title;
	}



    public String getId() { return this.id; }
	
	public int getIcon(){
		return this.icon;
	}
	
	public String getCount(){
        return this.count;
	}

    public boolean isProfile() {
        return this.isProfile;
    }

    public boolean isDivider() {
        return isDivider;
    }

    public boolean isOption() {
        return isOption;
    }

    public boolean getCounterVisibility(){
		return this.isCounterVisible;
	}
	
	public void setTitle(String title){
		this.title = title;
	}

    public void setId(String id){
        this.id = id;
    }

	public void setIcon(int icon){
		this.icon = icon;
	}
	
	public void setCount(String count){
		this.count = count;
		if (!count.equals("0"))
			isCounterVisible = true;
		else
			isCounterVisible = false;
	}
	
	public void setCounterVisibility(boolean isCounterVisible){
		this.isCounterVisible = isCounterVisible;
	}
}