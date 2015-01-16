package com.fsz570.easyaccounting;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import com.fsz570.db_utils.DBAdapter;
import com.fsz570.easyaccounting.adapter.CategoryExpandableListAdapter;
import com.fsz570.easyaccounting.vo.CategoryVo;

import java.util.List;

public class UpdateCategoryActivity extends Activity {

	private static final String TAG = "UpdateCategoryActivity";

	// Database
	DBAdapter dbAdapter = null;
	ExpandableListView listView;
	CategoryExpandableListAdapter listViewAdapter;
	List<CategoryVo> categoryList;
	Context context;
	
	private View lastSelectedView;
	private int NO_VALUE = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update_category);
		context = this;

		initDB();
		initUi();
	}

	private void initUi() {
		initListView();
	}
	
	public void dismissActivity(View v){
		finish();
	}
	
	private void initListView(){
		listView = (ExpandableListView)findViewById(R.id.updaye_category_expandable_listview);
		
		categoryList = dbAdapter.getCategories();
		listViewAdapter = new CategoryExpandableListAdapter(this, categoryList);
		listView.setAdapter(listViewAdapter);
		listView.setGroupIndicator(null);
		
		listView.setOnGroupClickListener(new OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

            	Log.d(TAG, "onGroupClick()" );
            	
                //CategoryVo vo = (CategoryVo)v.getTag();
                CategoryVo vo = categoryList.get(groupPosition);
                ImageButton upBtn = (ImageButton)v.findViewById(R.id.category_up_btn);
                ImageButton downBtn = (ImageButton)v.findViewById(R.id.category_down_btn);
                
                Log.d(TAG, "vo.getTranCategoryName() : " + vo.getTranCategoryName());
                Log.d(TAG, "vo.isShowUpBtn() : " + vo.isShowUpBtn());
                Log.d(TAG, "vo.isShowDownBtn() : " + vo.isShowDownBtn());
                
                if(vo.getId() == CategoryVo.NEW_CATEGORY_ID){
                	showNewGroupCategoryDialog();
                }else{
	                if(upBtn.getVisibility() == View.GONE || downBtn.getVisibility() == View.GONE){

	                	hideButtonOnLastSelectedListItem();
	                	
						setItemButtonVisiblity(View.VISIBLE, vo, upBtn);
						setItemButtonVisiblity(View.VISIBLE, vo, downBtn);
						vo.setShowUpBtn(true);
						vo.setShowDownBtn(true);
						
						lastSelectedView = v;
	                }else{
	                	setItemButtonVisiblity(View.GONE, vo, upBtn);
	                	setItemButtonVisiblity(View.GONE, vo, downBtn);
	                	
	                	vo.setShowUpBtn(false);
	                	vo.setShowDownBtn(false);
	                	
	                	lastSelectedView = null;
	                }
                }

                return true;
            }
        });
		
		listView.setOnChildClickListener(new OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int position, long id) {
                
            	ImageButton upBtn = (ImageButton) v.findViewById(R.id.category_up_btn);
            	ImageButton downBtn = (ImageButton)v.findViewById(R.id.category_down_btn);
            	
            	CategoryVo parentCategoryVo = categoryList.get(groupPosition);
            	//CategoryVo vo = (CategoryVo)v.getTag();
            	CategoryVo vo = parentCategoryVo.getChildCategory().get(position);
            	
            	if(vo.getId() == CategoryVo.NEW_CATEGORY_ID){
            		showNewChildCategoryDialog(parentCategoryVo.getId());
                }else{
                
                	if(upBtn.getVisibility() == View.GONE || downBtn.getVisibility() == View.GONE){
	                	
	                	hideButtonOnLastSelectedListItem();
	                	
                		setItemButtonVisiblity(View.VISIBLE, vo, upBtn);
                		setItemButtonVisiblity(View.VISIBLE, vo, downBtn);
                		vo.setShowUpBtn(true);
                		vo.setShowDownBtn(true);
                		
                		lastSelectedView = v;
	                }else{
	                	setItemButtonVisiblity(View.GONE, vo, upBtn);
	                	setItemButtonVisiblity(View.GONE, vo, downBtn);
	                	
	                	vo.setShowUpBtn(false);	                	
	                	vo.setShowDownBtn(false);
	                	
	                	lastSelectedView = null;
	                }
                }

                return true;
            }
        });
		
		registerForContextMenu(listView);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);

	    ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;

	    int type = ExpandableListView.getPackedPositionType(info.packedPosition);
	    int groupPosition = ExpandableListView.getPackedPositionGroup(info.packedPosition);
	    int childPosition = ExpandableListView.getPackedPositionChild(info.packedPosition);

	    // Show context menu for groups
	    if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
	    	CategoryVo groupCategory = categoryList.get(groupPosition);
	        menu.setHeaderTitle(groupCategory.getTranCategoryName());
	        menu.add(Menu.NONE, Menu.NONE, 1, context.getString(R.string.update_category));

	        // Show context menu for children
	    } else if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
	    	CategoryVo groupCategory = categoryList.get(groupPosition);
	    	CategoryVo childCategory = groupCategory.getChildCategory().get(childPosition);
	        menu.setHeaderTitle(childCategory.getTranCategoryName());
	        menu.add(Menu.NONE, Menu.NONE, 1, context.getString(R.string.update_category));
	    }
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item
	            .getMenuInfo();

	    int type = ExpandableListView.getPackedPositionType(info.packedPosition);
	    int groupPosition = ExpandableListView.getPackedPositionGroup(info.packedPosition);
	    int childPosition = ExpandableListView.getPackedPositionChild(info.packedPosition);

	    if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
	        // do something with parent
	    	//Toast.makeText(context, "Group : " + groupPosition, Toast.LENGTH_SHORT).show();
	    	showUpdateGroupCategoryDialog(categoryList.get(groupPosition));

	    } else if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
	        // do something with child
	    	//Toast.makeText(context, "Group : " + groupPosition + ", Child : " + childPosition, Toast.LENGTH_SHORT).show();
	    	showUpdateGroupCategoryDialog(categoryList.get(groupPosition).getChildCategory().get(childPosition));
	    }

	    return super.onContextItemSelected(item);
	}
	
	private void hideButtonOnLastSelectedListItem() {
		
		Log.d(TAG, "hideButtonOnLastSelectedListItem()");
		Log.d(TAG, "lastSelectedView exists? " + (lastSelectedView != null));

		if(lastSelectedView != null ){
			Log.d(TAG, "Last selected category : " + ((CategoryVo)lastSelectedView.getTag()).getTranCategoryName());
			((ImageButton)lastSelectedView.findViewById(R.id.category_up_btn)).setVisibility(View.GONE);
			((ImageButton)lastSelectedView.findViewById(R.id.category_down_btn)).setVisibility(View.GONE);
			
			((CategoryVo)lastSelectedView.getTag()).setShowUpBtn(false);
			((CategoryVo)lastSelectedView.getTag()).setShowDownBtn(false);
		}
		
		lastSelectedView = null;
	}
	
	private void setItemButtonVisiblity(int visiblity, CategoryVo vo, View button){
		if (visiblity == View.VISIBLE && (vo.getId() != CategoryVo.NEW_CATEGORY_ID)) {
			button.setVisibility(View.VISIBLE);
			button.setFocusable(false);
			button.setTag(vo);
		} else if (visiblity == View.GONE) {
			button.setVisibility(View.GONE);
		}
	}
	
	public static class UpdateCategoryAlertDialogFragment extends DialogFragment {

	    public static UpdateCategoryAlertDialogFragment newInstance(int title, boolean isChild, int parentId, boolean isUpdate, CategoryVo originalCategoryVo) {
	    	UpdateCategoryAlertDialogFragment frag = new UpdateCategoryAlertDialogFragment();
	        Bundle args = new Bundle();
	        args.putInt("title", title);
	        args.putBoolean("isChild", isChild);
	        args.putInt("parentId", parentId);
	        args.putBoolean("isUpdate", isUpdate);
	        args.putParcelable("originalCategoryVo", originalCategoryVo);
	        frag.setArguments(args);
	        return frag;
	    }

	    @Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	    	final int title = getArguments().getInt("title");
	    	final boolean isChild = getArguments().getBoolean("isChild");
	        final int parentId = getArguments().getInt("parentId");
	        final boolean isUpdate = getArguments().getBoolean("isUpdate");
	        final CategoryVo originalCategoryVo = getArguments().getParcelable("originalCategoryVo");
	        
	        EditText categoryNameEditText = new EditText(getActivity());
	        categoryNameEditText.setId(CategoryVo.UPDATE_CATEGORY_TEXT_ID); //Not make any sense, just need a int here

	        //If add Group Category
	        if(!isUpdate && !isChild){
	        return new AlertDialog.Builder(getActivity())
	                .setTitle(title)
	                .setView(categoryNameEditText)
	                .setPositiveButton(R.string.btn_confirm_text,
	                    new DialogInterface.OnClickListener() {
	                        public void onClick(DialogInterface dialog, int whichButton) {
	                        	EditText categoryNameEditText = (EditText)((AlertDialog)dialog).findViewById(CategoryVo.UPDATE_CATEGORY_TEXT_ID);
	                            ((UpdateCategoryActivity)getActivity()).doNewGroupCategoryClick(categoryNameEditText.getText().toString());
	                        }
	                    }
	                )
	                .setNegativeButton(R.string.btn_cancel_text,
	                    new DialogInterface.OnClickListener() {
	                        public void onClick(DialogInterface dialog, int whichButton) {
	                            ((UpdateCategoryActivity)getActivity()).doCancelClick();
	                        }
	                    }
	                )
	                .create();
	        //If add Child Category
	        }else if(!isUpdate && isChild){
		        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setView(categoryNameEditText)
                .setPositiveButton(R.string.btn_confirm_text,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        	EditText eventNameEditText = (EditText)((AlertDialog)dialog).findViewById(CategoryVo.UPDATE_CATEGORY_TEXT_ID);
                            ((UpdateCategoryActivity)getActivity()).doNewChildCategoryClick(parentId, eventNameEditText.getText().toString());
                        }
                    }
                )
                .setNegativeButton(R.string.btn_cancel_text,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            ((UpdateCategoryActivity)getActivity()).doCancelClick();
                        }
                    }
                )
                .create();
		    //If update Category
	        }else if(isUpdate){
	        	categoryNameEditText.setText(originalCategoryVo.getTranCategoryName());
	        	
	        	return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setView(categoryNameEditText)
                .setPositiveButton(R.string.btn_confirm_text,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        	EditText eventNameEditText = (EditText)((AlertDialog)dialog).findViewById(CategoryVo.UPDATE_CATEGORY_TEXT_ID);
                            ((UpdateCategoryActivity)getActivity()).doUpdateCategoryClick(originalCategoryVo.getId(), eventNameEditText.getText().toString());
                        }
                    }
                )
                .setNegativeButton(R.string.btn_cancel_text,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            ((UpdateCategoryActivity)getActivity()).doCancelClick();
                        }
                    }
                )
                .create();
	        }else{
	        	return null;
	        }
	    }
	}
	
	
	public void doNewGroupCategoryClick(String newCategoryName) {
	    // Do stuff here.
		dbAdapter.newParentCategory(newCategoryName);
		listViewAdapter.setDataSource(dbAdapter.getCategories()); //Refresh after update
	}
	
	public void doNewChildCategoryClick(int parentId, String newCategoryName) {
	    // Do stuff here.
		dbAdapter.newChildCategory(parentId, newCategoryName);
		listViewAdapter.setDataSource(dbAdapter.getCategories()); //Refresh after update
	}
	
	public void doUpdateCategoryClick(int id, String newCategoryName) {
	    // Do stuff here.
		dbAdapter.updateCategory(id, newCategoryName);
		listViewAdapter.setDataSource(dbAdapter.getCategories()); //Refresh after update
	}
	
	public void doCancelClick() {
	    // Do stuff here.
		Toast.makeText(this, getResources().getString(R.string.btn_cancel_text), Toast.LENGTH_SHORT).show();
	}
	
	public void showNewGroupCategoryDialog() {
	    DialogFragment newFragment = UpdateCategoryAlertDialogFragment.newInstance(
	            R.string.action_new_category_title, false, NO_VALUE, false, null);
	    newFragment.show(getFragmentManager(), "dialog");
	}
	
	public void showNewChildCategoryDialog(int parentId) {
		DialogFragment newFragment = UpdateCategoryAlertDialogFragment.newInstance(
	            R.string.action_new_category_title, true, parentId, false, null);
	    newFragment.show(getFragmentManager(), "dialog");
	}
	
	public void showUpdateGroupCategoryDialog(CategoryVo originalCategoryVo) {
	    DialogFragment newFragment = UpdateCategoryAlertDialogFragment.newInstance(
	            R.string.action_update_category_title, false, NO_VALUE, true, originalCategoryVo);
	    newFragment.show(getFragmentManager(), "dialog");
	}
	
	public void updateCategorySeq(List<CategoryVo> paretnCategoryList){
		
		int parentIndex = 0;
		int childIndex = 0;
		for(CategoryVo parentCategoryVo:paretnCategoryList){
			
			for(CategoryVo childCategoryVo:parentCategoryVo.getChildCategory()){
				if(childCategoryVo.getId() != CategoryVo.NEW_CATEGORY_ID){
					dbAdapter.updateCategory(childCategoryVo.getId(), childIndex++);
				}
			}
			if(parentCategoryVo.getId() != CategoryVo.NEW_CATEGORY_ID){
				dbAdapter.updateCategory(parentCategoryVo.getId(), parentIndex++);
			}
		}
	}
	
	public void setLastSelectedView(View view){
		lastSelectedView = view;
	}

	private void initDB() {
		Log.d(TAG, "initDB() start");
		// Instant the DB Adapter will create the DB is it not exist.
		dbAdapter = new DBAdapter(UpdateCategoryActivity.this);

		// code that needs 6 seconds for execution
		try {
			dbAdapter.createDataBase();

		} catch (Exception e) {
			Log.d(TAG, "initDB() Exception");
			Log.d(TAG, e.getMessage());
		} finally {
			dbAdapter.close();
		}
		// after finishing, close the progress bar
		Log.d(TAG, "initDB() end.");
	}

	public DBAdapter getDbAdapter() {
		return dbAdapter;
	}

	public void setDbAdapter(DBAdapter dbAdapter) {
		this.dbAdapter = dbAdapter;
	}
	
	
}
