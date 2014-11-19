package com.tesladocet.threedprinting.printers;

import android.os.Parcel;
import android.os.Parcelable;

public class Printer3D implements Parcelable {
	
	public final static String EXTRA_PRINTER = 
			"com.tesladocet.threedprinting.printers.EXTRA_PRINTER";
	
	private String id;
	private String name;
	private String description;
	private String location;
	
	private String type;
	private String technology;
	private PrintAttributes attributes;
	private Link link;
	
	private int status;
	
	public Printer3D(String id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public Printer3D(Parcel in) {
		id = in.readString();
		name = in.readString();
		description = in.readString();
		location = in.readString();
		
		type = in.readString();
		technology = in.readString();
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public PrintAttributes getAttributes() {
		return attributes;
	}

	public void setAttributes(PrintAttributes attributes) {
		this.attributes = attributes;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public String getTechnology() {
		return technology;
	}

	public Link getLink() {
		return link;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(id);
		out.writeString(name);
		out.writeString(description);
		out.writeString(location);
		
		out.writeString(type);
		out.writeString(technology);
		//PrinterOptions options;
		//Link link;
	}
	
	public final static Parcelable.Creator<Printer3D> CREATOR = 
			new Parcelable.Creator<Printer3D>() {
        @Override
        public Printer3D createFromParcel(Parcel source) {
            return new Printer3D(source);
        }

        @Override
        public Printer3D[] newArray(int size) {
            return new Printer3D[size];
        }
    };
}