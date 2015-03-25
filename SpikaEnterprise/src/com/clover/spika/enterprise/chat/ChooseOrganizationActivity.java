package com.clover.spika.enterprise.chat;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.clover.spika.enterprise.chat.adapters.OrganizationAdapter;
import com.clover.spika.enterprise.chat.extendables.LoginBaseActivity;
import com.clover.spika.enterprise.chat.models.Organization;
import com.clover.spika.enterprise.chat.utils.Const;

public class ChooseOrganizationActivity extends LoginBaseActivity implements OnItemClickListener{
	
	private ListView orgListView;
	private OrganizationAdapter adapter;
	
	private List<Organization> organizations = new ArrayList<Organization>();
	private Bundle extras;
	private String username;
	private String password;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_choose_organization);
		
		extras = getIntent().getExtras();
		organizations = (List<Organization>) this.getIntent().getSerializableExtra(Const.ORGANIZATIONS);
		username = extras.getString(Const.USERNAME);
		password = extras.getString(Const.PASSWORD);
		
		orgListView = (ListView) findViewById(R.id.listOrganizations);
		adapter = new OrganizationAdapter(this);
		
		orgListView.setAdapter(adapter);
		orgListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		adapter.setData(organizations);
		
		findViewById(R.id.goBack).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(ChooseOrganizationActivity.this, LoginActivity.class));
				finish();
			}
		});
		
		orgListView.setOnItemClickListener(this);
	}
	

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		
		Organization organization = organizations.get(position);
		
		login(organization.id);
		
	}
	
	
	private void login(String organizationId){
		
		try {
			executeLoginApi(username, password, organizationId, extras, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
