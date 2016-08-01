/*
* Copyright 2013 Evgeny Shishkin
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.neat.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ScaleXSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import com.neat.activity.R;

public class RobotoTextView extends TextView {

	public final static float NORMAL = 0;
    private float letterSpacing = NORMAL;
    private CharSequence originalText = "";

    public RobotoTextView(Context context) {
        super(context);
    }

    public RobotoTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseAttributes(context, attrs);
    }

    public RobotoTextView(Context context, AttributeSet attrs, int defStyle) {
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
    	letterSpacing = values.getFloat(R.styleable.TextView_textSpacing, NORMAL);
    	values.recycle();

        originalText = getText();
        applyLetterSpacing();
    	setTypeface(obtainTypeface(context, typefaceValue));
    }
    
    public float getLetterSpacing() {
        return letterSpacing;
    }

    public void setLetterSpacing(float letterSpacing) {
        this.letterSpacing = letterSpacing;
        applyLetterSpacing();
    }
    
    private void applyLetterSpacing() {
    	if(getLetterSpacing() == NORMAL){
    		return;
    	}
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < originalText.length(); i++) {
            builder.append(originalText.charAt(i));
            if(i+1 < originalText.length()) {
                builder.append("\u00A0");
            }
        }
        SpannableString finalText = new SpannableString(builder.toString());
        if(builder.toString().length() > 1) {
            for(int i = 1; i < builder.toString().length(); i+=2) {
                finalText.setSpan(new ScaleXSpan((getLetterSpacing()+1)/10), i, i+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        super.setText(finalText, BufferType.SPANNABLE);
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
}
