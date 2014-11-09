package com.tesladocet.threedprinting.printers;

import com.tesladocet.threedprinting.R;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

public class MaterialView extends LinearLayout {

	View filament;
	float DENSITY = 0;
	
	public MaterialView(Context context) {
		super(context);
		init();
	}
	
	public MaterialView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public MaterialView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}
	
	private void init() {
		DENSITY = getContext().getResources().getDisplayMetrics().density;
		View container = inflate(getContext(), R.layout.material_view, this);
		filament = (View) container.findViewById(R.id.filament);
		
		GradientDrawable background = (GradientDrawable) getResources()
				.getDrawable(R.drawable.filament);
		background.setColor(Color.GREEN);
		background.setCornerRadius(8 * DENSITY);
		
		filament.setBackground(background);
	}
}