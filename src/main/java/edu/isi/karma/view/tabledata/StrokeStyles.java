/**
 * 
 */
package edu.isi.karma.view.tabledata;

import edu.isi.karma.view.Stroke.StrokeStyle;
import edu.isi.karma.view.tabledata.VDCell.Position;

/**
 * @author szekely
 * 
 */
public class StrokeStyles {
	private StrokeStyle left = StrokeStyle.none;
	private StrokeStyle right = StrokeStyle.none;
	private StrokeStyle top = StrokeStyle.none;
	private StrokeStyle bottom = StrokeStyle.none;

	public StrokeStyles() {
		super();
	}

	public void setStrokeStyle(Position position, StrokeStyle strokeStyle) {
		switch (position) {
		case left:
			this.left = strokeStyle;
			return;
		case right:
			this.right = strokeStyle;
			return;
		case top:
			this.top = strokeStyle;
			return;
		case bottom:
			this.bottom = strokeStyle;
			return;
		}
	}

	public String getJsonEncoding() {
		return left.code() + ":" + right.code() + ":" + top.code() + ":"
				+ bottom.code();
	}
}
