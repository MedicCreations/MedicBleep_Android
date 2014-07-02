package jp.co.vector.chat.adapters;

import java.util.ArrayList;
import java.util.List;

import jp.co.vector.chat.lazy.ImageLoader;
import jp.co.vector.chat.model.Character;
import jp.co.vector.chat.utils.Helper;
import jp.co.vector.chat.view.ViewHolderCharacter;

import jp.co.vector.chat.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class CharacterAdapter extends BaseAdapter {

    Context cntx;
    List<Character> data;
    List<String> selectedIds = new ArrayList<String>();

    ImageLoader imageLoader;

    boolean isSelect = false;

    public CharacterAdapter(Context context, ArrayList<Character> arrayList) {
	this.cntx = context;
	this.data = arrayList;

	imageLoader = new ImageLoader(context);
    }

    @Override
    public int getCount() {
	return data.size();
    }

    @Override
    public Character getItem(int position) {
	return data.get(position);
    }

    @Override
    public long getItemId(int position) {
	return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

	final ViewHolderCharacter holder;
	if (convertView == null) {

	    convertView = LayoutInflater.from(cntx).inflate(R.layout.profile_person_item_layout, null);

	    holder = new ViewHolderCharacter(convertView);
	    convertView.setTag(holder);
	} else {
	    holder = (ViewHolderCharacter) convertView.getTag();
	}

	// set image to null
	holder.profileImg.setImageDrawable(null);

	// Assign values
	final Character profile = (Character) getItem(position);

	if (!isSelect) {
	    holder.selectableImg.setVisibility(View.GONE);
	} else {
	    holder.selectableImg.setVisibility(View.VISIBLE);

	    holder.itemCliclkLayout.setTag(position);
	    holder.itemCliclkLayout.setOnClickListener(new View.OnClickListener() {

		@Override
		public void onClick(View view) {

		    if (data.get((Integer) view.getTag()).isSelected()) {
			data.get((Integer) view.getTag()).setSelected(false);
		    } else {
			data.get((Integer) view.getTag()).setSelected(true);
		    }

		    selectedIdsEdit(data.get((Integer) view.getTag()).getCharacterId());

		    notifyDataSetChanged();
		}
	    });

	    if (profile.isSelected()) {
		holder.selectableImg.setImageDrawable(cntx.getResources().getDrawable(R.drawable.gb_tableview_edit_selected));
	    } else {
		holder.selectableImg.setImageDrawable(cntx.getResources().getDrawable(R.drawable.gb_tableview_edit_deselected));
	    }
	}

	holder.personName.setText(Helper.substringText(profile.getUsername(), 25));

	imageLoader.displayImage(profile.getImage_name(), holder.profileImg, true);

	return convertView;
    }

    public void addItems(List<Character> newItems) {
	data.addAll(newItems);
	this.notifyDataSetChanged();
    }

    public void clearItems() {
	data.clear();
	selectedIds.clear();
	notifyDataSetChanged();
    }

    public boolean isSelect() {
	return isSelect;
    }

    public void setSelect(boolean isSelect) {
	this.isSelect = isSelect;
    }

    private void selectedIdsEdit(String id) {

	boolean isFound = false;

	if (selectedIds == null) {
	    selectedIds = new ArrayList<String>();
	}

	for (String inId : selectedIds) {
	    if (inId.equals(id)) {
		isFound = true;
		break;
	    }
	}

	if (isFound) {
	    removeId(id);
	} else {
	    addId(id);
	}
    }

    private void addId(String id) {
	selectedIds.add(id);
    }

    private void removeId(String id) {
	selectedIds.remove(id);
    }

    public String[] getSelectedIds() {
	return selectedIds.toArray(new String[selectedIds.size()]);
    }

}