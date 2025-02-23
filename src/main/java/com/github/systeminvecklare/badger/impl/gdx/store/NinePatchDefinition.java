package com.github.systeminvecklare.badger.impl.gdx.store;

public class NinePatchDefinition {
	public final String textureName;
	public final int left;
	public final int right;
	public final int top;
	public final int bottom;
	
	public NinePatchDefinition(String textureName, int inset) {
		this(textureName, inset, inset);
	}
	
	public NinePatchDefinition(String textureName, int horizontal, int vertical) {
		this(textureName, horizontal, horizontal, vertical, vertical);
	}

	public NinePatchDefinition(String textureName, int left, int right, int top, int bottom) {
		this.textureName = textureName;
		this.left = left;
		this.right = right;
		this.top = top;
		this.bottom = bottom;
	}
	
	@Override
	public int hashCode() {
		return textureName.hashCode() ^ top ^ bottom ^ left ^ right;
	}
	
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof NinePatchDefinition) && equalsOwn((NinePatchDefinition) obj);
	}

	private boolean equalsOwn(NinePatchDefinition obj) {
		return this.textureName.equals(obj.textureName)
				&& this.left == obj.left
				&& this.right == obj.right
				&& this.top == obj.top
				&& this.bottom == obj.bottom;
	}

}
