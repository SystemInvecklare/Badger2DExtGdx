package com.github.systeminvecklare.badger.impl.gdx.store.atlas;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.IntConsumer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture.TextureFilter;

public class AtlasBuilder implements IAtlasBuilder {
	private static final Comparator<Sides> BIGGEST_MIN_SIDE_FIRST_COMPARATOR = new Comparator<Sides>() {
		@Override
		public int compare(Sides o1, Sides o2) {
			int minCompare = Integer.compare(o1.min, o2.min);
			if(minCompare == 0) {
				return -Integer.compare(o1.max, o2.max);
			} else {
				return -minCompare;
			}
		}
	};
	private static final Comparator<Sides> BIGGEST_MAX_SIDE_FIRST_COMPARATOR = new Comparator<Sides>() {
		@Override
		public int compare(Sides o1, Sides o2) {
			int maxCompare = Integer.compare(o1.max, o2.max);
			if(maxCompare == 0) {
				return Integer.compare(o1.min, o2.min);
			} else {
				return -maxCompare;
			}
		}
	};
	
	private final List<String> textures;
	private final int width;
	private final int height;
	private final int padding;
	private WeightCalculation weightCalculation = WeightCalculation.SUM;
	private PreSorting preSorting = PreSorting.BIGGEST_MAX_SIDE_FIRST;
	private IntConsumer tightnessConsumers = null;
	private boolean allowOverflow = false;
	private TextureFilter minFilter = TextureFilter.Linear;
	private TextureFilter magFilter = TextureFilter.Linear;
	
	public AtlasBuilder(List<String> textures) {
		this(2048, textures);
	}
	
	public AtlasBuilder(int size, List<String> textures) {
		this(size, size, textures);
	}
	
	public AtlasBuilder(int width, int height, List<String> textures) {
		this(width, height, 1, textures);
	}
	
	public AtlasBuilder(int width, int height, int padding, List<String> textures) {
		this.width = width;
		this.height = height;
		this.padding = padding;
		this.textures = textures;
	}
	
	public AtlasBuilder weightMin() {
		weightCalculation = WeightCalculation.MIN;
		return this;
	}
	
	public AtlasBuilder weightMax() {
		weightCalculation = WeightCalculation.MAX;
		return this;
	}
	
	public AtlasBuilder weightSum() {
		weightCalculation = WeightCalculation.SUM;
		return this;
	}
	
	public AtlasBuilder sortBiggestMinSideFirst() {
		preSorting = PreSorting.BIGGEST_MIN_SIDE_FIRST;
		return this;
	}
	
	public AtlasBuilder sortBiggestMaxSideFirst() {
		preSorting = PreSorting.BIGGEST_MAX_SIDE_FIRST;
		return this;
	}
	
	public AtlasBuilder sortNone() {
		preSorting = PreSorting.NONE;
		return this;
	}
	
	public AtlasBuilder allowOverflow() {
		this.allowOverflow = true;
		return this;
	}
	
	public AtlasBuilder minFilter(TextureFilter textureFilter) {
		this.minFilter = textureFilter;
		return this;
	}
	
	public AtlasBuilder magFilter(TextureFilter textureFilter) {
		this.magFilter = textureFilter;
		return this;
	}
	
	public AtlasBuilder collectTightness(IntConsumer tightnessConsumer) {
		if(this.tightnessConsumers == null) {
			this.tightnessConsumers = tightnessConsumer;
		} else if(this.tightnessConsumers instanceof ConsumerList) {
			((ConsumerList) (this.tightnessConsumers)).intConsumers.add(tightnessConsumer);
		} else {
			ConsumerList consumerList = new ConsumerList();
			consumerList.intConsumers.add(this.tightnessConsumers);
			consumerList.intConsumers.add(tightnessConsumer);
			this.tightnessConsumers = consumerList;
		}
		return this;
	}
	
	@Override
	public TextureFilter getMinFilter() {
		return minFilter;
	}
	
	@Override
	public TextureFilter getMagFilter() {
		return magFilter;
	}

	@Override
	public void build(IAtlasConstruction construction) {
		preSort();
		
		List<String> overflowTextures = allowOverflow ? new ArrayList<String>(0) : null;
		int minOverflowSize = Integer.MAX_VALUE;
		Region rootRegion = new Region(0, 0, getAltasWidth(), getAltasHeight());
		for(String texture : textures) {
			Pixmap pixmap = new Pixmap(Gdx.files.internal(texture));
			boolean gotPlaced = rootRegion.place(texture, pixmap, padding, construction);
			if(!gotPlaced) {
				if(!allowOverflow) {
					pixmap.dispose();
					throw new RuntimeException("No space for "+texture);
				} else {
					Sides sides = new Sides(texture, pixmap.getWidth(), pixmap.getHeight());
					minOverflowSize = Math.min(minOverflowSize, sides.max);
					overflowTextures.add(sides.texture);
					pixmap.dispose();
				}
			}
		}
		if(allowOverflow && !overflowTextures.isEmpty()) {
			int overflowSize = Math.min(width, height);
			if(overflowSize < minOverflowSize) {
				StringBuilder names = new StringBuilder();
				for(String texture : overflowTextures) {
					names.append(texture).append(", ");
				}
				throw new RuntimeException("Can't overflow! "+names+" are all too big for "+overflowSize);
			}
			AtlasBuilder overflowAtlas = new AtlasBuilder(overflowSize, overflowTextures);
			overflowAtlas.allowOverflow = true;
			overflowAtlas.weightCalculation = this.weightCalculation;
			overflowAtlas.preSorting = PreSorting.NONE; //Already sorted
			overflowAtlas.tightnessConsumers = tightnessConsumers;
			
			construction.setOverflowAtlas(overflowAtlas);
		}
		if(tightnessConsumers != null) {
			tightnessConsumers.accept(rootRegion.getTightness());
		}
	}

	private void preSort() {
		switch(preSorting) {
			case BIGGEST_MIN_SIDE_FIRST: {
				sortTexturesBySides(textures, BIGGEST_MIN_SIDE_FIRST_COMPARATOR);
			} break;
			case BIGGEST_MAX_SIDE_FIRST: {
				sortTexturesBySides(textures, BIGGEST_MAX_SIDE_FIRST_COMPARATOR);
			} break;
			case NONE: break;
		}
	}
	
	private static void sortTexturesBySides(List<String> textures, Comparator<Sides> comparator) {
		List<Sides> sortMap = new ArrayList<Sides>(textures.size());
		for(String texture : textures) {
			Pixmap pixmap = new Pixmap(Gdx.files.internal(texture));
			sortMap.add(new Sides(texture, pixmap.getWidth(), pixmap.getHeight()));
			pixmap.dispose();
		}
		textures.clear();
		sortMap.sort(comparator);
		for(Sides sorted : sortMap) {
			textures.add(sorted.texture);
		}
	}

	@Override
	public int getAltasWidth() {
		return width;
	}

	@Override
	public int getAltasHeight() {
		return height;
	}

	@Override
	public boolean contains(String texture) {
		return textures.contains(texture);
	}
	
	// Contract: 1) Symmetric for w vs h. 2) squareDistance(w,h) == squareDistance(a*w, a*h), all a > 0 (scale invariance)
	private static float squareDistance(int w, int h) {
		if(w <= 0 || h <= 0) {
			return -1f;
		}
		return ((float)(w + h))/(2f*((float) Math.min(w, h)));
	}
	
	private class Region {
		private final int x;
		private final int y;
		private final int width;
		private final int height;
		private boolean used = false;
		private Region right = null;
		private Region bottom = null;
		
		public Region(int x, int y, int width, int height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}
		
		public int getTightness() {
			// Calculates the max sized square that would fit.
			if(!used) {
				return Math.min(width, height);
			} else {
				if(right != null) {
					if(bottom != null) {
						return Math.max(right.getTightness(), bottom.getTightness());
					} else {
						return right.getTightness();
					}
				} else {
					if(bottom != null) {
						return bottom.getTightness(); 
					} else {
						return 0;
					}
				}				
			}
		}

		private int getDepth() {
			if(right == null) {
				if(bottom == null) {
					return 0;
				} else {
					return bottom.getDepth() + 1;
				}
			} else {
				if(bottom == null) {
					return right.getDepth() + 1;
				} else {
					switch(weightCalculation) {
						case MAX:
							return Math.max(right.getDepth(), bottom.getDepth()) + 1;
						case MIN:
							return Math.min(right.getDepth(), bottom.getDepth()) + 1;
						case SUM:
							return right.getDepth() + bottom.getDepth() + 1;
					}
					throw new RuntimeException("Non-exhaustive switch");
				}
			}
		}

		public boolean place(String name, Pixmap pixmap, int padding, IAtlasConstruction construction) {
			if(used) {
				// Try to balance.
				if(right != null) {
					if(bottom != null) {
						// If we have both
						if(right.getDepth() <= bottom.getDepth()) {
							if(right.place(name, pixmap, padding, construction)) {
								return true;
							}
							if(bottom.place(name, pixmap, padding, construction)) {
								return true;
							}
						} else {
							if(bottom.place(name, pixmap, padding, construction)) {
								return true;
							}
							if(right.place(name, pixmap, padding, construction)) {
								return true;
							}
						}
					} else {
						if(right.place(name, pixmap, padding, construction)) {
							return true;
						}
					}
				} else {
					if(bottom != null) {
						if(bottom.place(name, pixmap, padding, construction)) {
							return true;
						}
					}
				}
				return false;
			} else {
				int paddedWidth = pixmap.getWidth() + 2*padding;
				int paddedHeight = pixmap.getHeight() + 2*padding;
				
				// Check if fits,
				if(paddedWidth <= width && paddedHeight <= height) {
					// It fits!
					construction.add(name, pixmap, x, y, padding);
					used = true;
					
					int extraWidth = width - paddedWidth;
					int extraHeight = height - paddedHeight;
					
					// Give the lower right corner to the region that will become the most square.
					float rightSquareDistance = squareDistance(extraWidth, height);
					float bottomSquareDistance = squareDistance(width, extraHeight);
					
					boolean giveToRight = rightSquareDistance > 0f;
					if(bottomSquareDistance < rightSquareDistance) {
						giveToRight = false;
					}
					
					if(extraWidth > 0) {
						right = new Region(x + paddedWidth, y, extraWidth, giveToRight ? height : paddedHeight);
					}
					if(extraHeight > 0) {
						bottom = new Region(x, y + paddedHeight, giveToRight ? paddedWidth : width, extraHeight);
					}
					return true;
				} else {
					return false;
				}
			}
		}
	}
	
	private enum WeightCalculation {
		MIN, MAX, SUM
	}
	
	private enum PreSorting {
		NONE, BIGGEST_MIN_SIDE_FIRST, BIGGEST_MAX_SIDE_FIRST
	}
	
	private static class Sides {
		private final String texture; 
		private final int min;
		private final int max;
		
		public Sides(String texture, int width, int height) {
			this.texture = texture;
			this.min = Math.min(width, height);
			this.max = Math.max(width, height);
		}
	}
	
	private static class ConsumerList implements IntConsumer {
		private final List<IntConsumer> intConsumers = new ArrayList<IntConsumer>(2);
		
		@Override
		public void accept(int value) {
			for(IntConsumer consumer : intConsumers) {
				consumer.accept(value);
			}
		}
	}
}
