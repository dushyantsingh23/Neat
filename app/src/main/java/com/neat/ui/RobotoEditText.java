package com.neat.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Build.VERSION;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.MovementMethod;
import android.util.AttributeSet;

import com.neat.activity.NeatApplication;
import com.neat.activity.R;
import com.rengwuxian.materialedittext.MaterialEditText;

public class RobotoEditText extends MaterialEditText {
	private boolean allCaps = false;
	private CharSequence originalText;

	private BufferType originalType;

	public RobotoEditText(Context context) {
		this(context, null);
	}

	public RobotoEditText(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.editTextStyle);
	}

	public RobotoEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
        parseAttributes(context, attrs);
	}

    /**
     * Parse the attributes.
     *
     * @param context The Context the widget is running in, through which it can access the current theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the widget.
     */
    private void parseAttributes(Context context, AttributeSet attrs) {
    	// Typeface.createFromAsset doesn't work in the layout editor, so skipping.
    	if (isInEditMode()) {
			return;
		}

    	TypedArray values = context.obtainStyledAttributes(attrs, R.styleable.TextView);
    	int typefaceValue = values.getInt(R.styleable.TextView_typeface, 4);
    	values.recycle();

    	setTypeface(obtainTypeface(context, typefaceValue));
    }

    /**
     * Obtain typeface.
     *
     * @param context       The Context the widget is running in, through which it can
     *                      access the current theme, resources, etc.
     * @param typefaceValue values ​​for the "typeface" attribute
     * @return Roboto {@link Typeface}
     * @throws IllegalArgumentException if unknown `typeface` attribute value.
     */
    private Typeface obtainTypeface(Context context, int typefaceValue) throws IllegalArgumentException {
        Typeface typeface = FontUtils.getTypefaces().get(typefaceValue);
        if (typeface == null) {
            typeface = FontUtils.createTypeface(context, typefaceValue);
            FontUtils.getTypefaces().put(typefaceValue, typeface);
        }
        return typeface;
    }
    
	@Override
	protected boolean getDefaultEditable() {
		return true;
	}

	@Override
	protected MovementMethod getDefaultMovementMethod() {
		return ArrowKeyMovementMethod.getInstance();
	}

	@Override
	public Editable getText() {
		return (Editable) super.getText();
	}

	/**
	 * Convenience for {@link Selection#setSelection(Spannable, int, int)}.
	 */
	public void setSelection(int start, int stop) {
		Selection.setSelection(getText(), start, stop);
	}

	/**
	 * Convenience for {@link Selection#setSelection(Spannable, int)}.
	 */
	public void setSelection(int index) {
		Selection.setSelection(getText(), index);
	}

	/**
	 * Convenience for {@link Selection#selectAll}.
	 */
	public void selectAll() {
		Selection.selectAll(getText());
	}

	/**
	 * Convenience for {@link Selection#extendSelection}.
	 */
	public void extendSelection(int index) {
		Selection.extendSelection(getText(), index);
	}

	@Override
	public void setEllipsize(TextUtils.TruncateAt ellipsis) {
		if (ellipsis == TextUtils.TruncateAt.MARQUEE) {
			throw new IllegalArgumentException(
					"RobotoNormalEditText cannot use the ellipsize mode "
							+ "TextUtils.TruncateAt.MARQUEE");
		}
		super.setEllipsize(ellipsis);
	}

	@Override
	@SuppressLint("NewApi")
	public void dispatchDisplayHint(int hint) {
		onDisplayHint(hint);
	}

	public boolean isAllCaps() {
		return allCaps;
	}

	@Override
	@SuppressLint("NewApi")
	protected void onDisplayHint(int hint) {
		if (VERSION.SDK_INT >= 8) {
			super.onDisplayHint(hint);
		}
	}

	@Override
	public void setAllCaps(boolean allCaps) {
		this.allCaps = allCaps;
		updateTextState();
	}
	
	@Override
	public void setText(CharSequence text, BufferType type) {
		originalText = text;
		originalType = type;
		updateTextState();
	}

	private void updateTextState() {
		if (originalText == null) {
			super.setText(null, originalType);
			return;
		}
		super.setText(allCaps ? originalText.toString().toUpperCase(NeatApplication.getInstance().getLocale())
				: originalText, originalType);
	}

}
