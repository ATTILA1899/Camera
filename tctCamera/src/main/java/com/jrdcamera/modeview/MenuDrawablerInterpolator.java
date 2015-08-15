package com.jrdcamera.modeview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.animation.Interpolator;

		public class MenuDrawablerInterpolator implements Interpolator {
				private final float mFactor;
				private final double mDoubleFactor;

				public MenuDrawablerInterpolator() {
					mFactor = 1.0f;
					mDoubleFactor = 2.0;
				}

				public MenuDrawablerInterpolator(float factor) {
					mFactor = factor;
					mDoubleFactor = 2 * mFactor;
				}

//				public MenuDrawablerInterpolator(Context context, AttributeSet attrs) {
//					TypedArray a =
//							context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.AccelerateInterpolator);
//
//					mFactor = a.getFloat(com.android.internal.R.styleable.AccelerateInterpolator_factor, 1.0f);
//					mDoubleFactor = 2 * mFactor;
//
//					a.recycle();
//				}

				@Override
				public float getInterpolation(float input) {
					if (mFactor == 1.0f) {
						return input * input;
					} else {
						return (float)Math.pow(input, mDoubleFactor);
					}
				}
			}

