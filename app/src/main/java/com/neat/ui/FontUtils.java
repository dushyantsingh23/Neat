package com.neat.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.TransformationMethod;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.util.SparseArray;
import android.view.View;

import com.neat.activity.NeatApplication;

import java.text.Normalizer;
import java.util.Locale;

public class FontUtils {

	public static final String TAG = "FontUtils";
	/*
	 * Permissible values ​​for the "typeface" attribute.
	 */
	public final static int THIN = 0;
	public final static int THIN_ITALIC = 1;
	public final static int LIGHT = 2;
	public final static int LIGHT_ITALIC = 3;
	public final static int REGULAR = 4;
	public final static int ITALIC = 5;
	public final static int MEDIUM = 6;
	public final static int MEDIUM_ITALIC = 7;
	public final static int BOLD = 8;
	public final static int BOLD_ITALIC = 9;
	public final static int BLACK = 10;
	public final static int BLACK_ITALIC = 11;
	public final static int CONDENSED = 12;
	public final static int CONDENSED_ITALIC = 13;
	public final static int CONDENSED_BOLD = 14;
	public final static int CONDENSED_BOLD_ITALIC = 15;
	public final static int ROBOTOSLAB_THIN = 16;
	public final static int ROBOTOSLAB_LIGHT = 17;
	public final static int ROBOTOSLAB_REGULAR = 18;
	public final static int ROBOTOSLAB_BOLD = 19;

	/**
	 * List of created typefaces for later reused.
	 */
	private final static SparseArray<Typeface> mTypefaces = new SparseArray<Typeface>(16);

	public FontUtils() {
	}

	public static SparseArray<Typeface> getTypefaces() {
		return mTypefaces;
	}

	/**
	 * Create typeface from assets.
	 * 
	 * @param context
	 *            The Context the widget is running in, through which it can
	 *            access the current theme, resources, etc.
	 * @param typefaceValue
	 *            values ​​for the "typeface" attribute
	 * @return Roboto {@link Typeface}
	 * @throws IllegalArgumentException
	 *             if unknown `typeface` attribute value.
	 */
	public static Typeface createTypeface(Context context, int typefaceValue) throws IllegalArgumentException {
		Typeface typeface;
		switch (typefaceValue) {
		case FontUtils.THIN:
			typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Thin.ttf");
			break;
		case FontUtils.THIN_ITALIC:
			typeface = Typeface.createFromAsset(context.getAssets(), "fonts/ThinItalic.ttf");
			break;
		case FontUtils.LIGHT:
			typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Light.ttf");
			break;
		case FontUtils.LIGHT_ITALIC:
			typeface = Typeface.createFromAsset(context.getAssets(), "fonts/LightItalic.ttf");
			break;
		case FontUtils.REGULAR:
			typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Regular.ttf");
			break;
		case FontUtils.ITALIC:
			typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Italic.ttf");
			break;
		case FontUtils.MEDIUM:
			typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Medium.ttf");
			break;
		case FontUtils.MEDIUM_ITALIC:
			typeface = Typeface.createFromAsset(context.getAssets(), "fonts/MediumItalic.ttf");
			break;
		case FontUtils.BOLD:
			typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Bold.ttf");
			break;
		case FontUtils.BOLD_ITALIC:
			typeface = Typeface.createFromAsset(context.getAssets(), "fonts/BoldItalic.ttf");
			break;
		case FontUtils.BLACK:
			typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Black.ttf");
			break;
		case FontUtils.BLACK_ITALIC:
			typeface = Typeface.createFromAsset(context.getAssets(), "fonts/BlackItalic.ttf");
			break;
		case FontUtils.CONDENSED:
			typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Condensed.ttf");
			break;
		case FontUtils.CONDENSED_ITALIC:
			typeface = Typeface.createFromAsset(context.getAssets(), "fonts/CondensedItalic.ttf");
			break;
		case FontUtils.CONDENSED_BOLD:
			typeface = Typeface.createFromAsset(context.getAssets(), "fonts/BoldCondensed.ttf");
			break;
		case FontUtils.CONDENSED_BOLD_ITALIC:
			typeface = Typeface.createFromAsset(context.getAssets(), "fonts/BoldCondensedItalic.ttf");
			break;
		case FontUtils.ROBOTOSLAB_THIN:
			typeface = Typeface.createFromAsset(context.getAssets(), "fonts/SlabThin.ttf");
			break;
		case FontUtils.ROBOTOSLAB_LIGHT:
			typeface = Typeface.createFromAsset(context.getAssets(), "fonts/SlabLight.ttf");
			break;
		case FontUtils.ROBOTOSLAB_REGULAR:
			typeface = Typeface.createFromAsset(context.getAssets(), "fonts/SlabRegular.ttf");
			break;
		case FontUtils.ROBOTOSLAB_BOLD:
			typeface = Typeface.createFromAsset(context.getAssets(), "fonts/SlabBold.ttf");
			break;
		default:
			throw new IllegalArgumentException("Unknown `typeface` attribute value " + typefaceValue);
		}
		return typeface;
	}

	public static class ShadowSpan extends CharacterStyle {
		public float dx;
		public float dy;
		public float radius;
		public int color;

		public ShadowSpan(float radius, float dx, float dy, int color) {
			this.radius = radius;
			this.dx = dx;
			this.dy = dy;
			this.color = color;
		}

		@Override
		public void updateDrawState(TextPaint tp) {
			//tp.setShadowLayer(radius, dx, dy, color);
		}
	}

	public static class StrikeColorSpan extends CharacterStyle implements ParcelableSpan {
		public static final int STRIKETHROUGH_SPAN = 5;
		public static final int FOREGROUND_COLOR_SPAN = 2;
		private final int mColor;

		public StrikeColorSpan(int color) {
			mColor = color;
		}

		public StrikeColorSpan(Parcel src) {
			mColor = src.readInt();
		}

		@Override
		public int getSpanTypeId() {
			return FOREGROUND_COLOR_SPAN;
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeInt(mColor);
		}

		@Override
		public void updateDrawState(TextPaint ds) {
			ds.setStrikeThruText(true);
			ds.setColor(mColor);
		}
	}

	@SuppressLint("NewApi")
	public static CharSequence highlight(Context context, String search, String originalText) {
		// ignore case and accents
		// the same thing should have been done for the search text
		String normalizedText = Normalizer.normalize(originalText, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
				.toLowerCase(NeatApplication.getInstance().getLocale());
		// StyleSpan styleSpan = new StyleSpan(android.graphics.Typeface.BOLD);
		// ForegroundColorSpan colorSpan = new
		// ForegroundColorSpan(context.getResources().getColor(R.color.searchTextColor));

		int start = normalizedText.indexOf(search);
		if (start < 0) {
			// not found, nothing to to
			return originalText;
		} else {
			// highlight each appearance in the original text
			// while searching in normalized text
			Spannable highlighted = new SpannableString(originalText);
			while (start >= 0) {
				int spanStart = Math.min(start, originalText.length());
				int spanEnd = Math.min(start + search.length(), originalText.length());

				highlighted.setSpan(new StyleSpan(Typeface.BOLD),
						spanStart,
						spanEnd,
						0);
				start = normalizedText.indexOf(search, spanEnd);
			}

			return highlighted;
		}
	}

	/**
	 * Transforms source text into an ALL CAPS string, locale-aware.
	 */
	public static class AllCapsTransformationMethod implements TransformationMethod {

		private final Locale mLocale;
		private boolean mEnabled;
		
		public AllCapsTransformationMethod(Context context) {
			mLocale = context.getResources().getConfiguration().locale;
		}

		@Override
		public CharSequence getTransformation(CharSequence source, View view) {
			if (mEnabled) {
				return source != null ? source.toString().toUpperCase(mLocale) : null;
			}
			return source;
		}

		@Override
		public void onFocusChanged(View view, CharSequence charSequence, boolean b, int i, Rect rect) {
		}
		
		public void setLengthChangesAllowed(boolean allowLengthChanges) {
			mEnabled = allowLengthChanges;
		}

	}
}