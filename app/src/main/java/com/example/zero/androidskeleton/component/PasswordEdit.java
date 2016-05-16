package com.example.zero.androidskeleton.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.example.zero.androidskeleton.R;
import com.example.zero.androidskeleton.utils.Utils;

/**
 * Created by zero on 5/15/16.
 */
public class PasswordEdit extends LinearLayout {

    private final String mHint;

    private EditText mEditText;

    public PasswordEdit(Context context) {
        this(context, null);
    }

    public PasswordEdit(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PasswordEdit(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PasswordEdit);
        mHint = typedArray.getString(R.styleable.PasswordEdit_hint);
        typedArray.recycle();

        setupUiComp(context);
    }

    private void setupUiComp(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.component_password_edit, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mEditText = (EditText) findViewById(R.id.password_edit_);
        assert mEditText != null;
        if (mHint != null) {
            mEditText.setHint(mHint);
        }

        final ImageView password_visible_img = (ImageView) findViewById(R.id.password_visible_img_);
        assert password_visible_img != null;
        if (Utils.isFlagSet(mEditText.getInputType(), InputType.TYPE_NUMBER_VARIATION_PASSWORD)) {
            password_visible_img.setImageResource(R.drawable.icon_password_gray_eye);
        } else {
            password_visible_img.setImageResource(R.drawable.icon_password_green_eye);
        }
        password_visible_img.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int type = mEditText.getInputType();
                if (Utils.isFlagSet(type, InputType.TYPE_NUMBER_VARIATION_PASSWORD)) {
                    mEditText.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    password_visible_img.setImageResource(R.drawable.icon_password_green_eye);
                } else {
                    mEditText.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                    password_visible_img.setImageResource(R.drawable.icon_password_gray_eye);
                }
                mEditText.setSelection(mEditText.getText().length());
            }
        });
    }

    public Editable getText() {
        return mEditText.getText();
    }

    public void addTextChangedListener(TextWatcher textWatcher) {
        mEditText.addTextChangedListener(textWatcher);
    }
}
