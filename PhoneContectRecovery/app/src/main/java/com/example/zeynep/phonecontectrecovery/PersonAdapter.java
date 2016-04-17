package com.example.zeynep.phonecontectrecovery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by zeynep on 13.04.2016.
 */

public class PersonAdapter extends ArrayAdapter<Person> {


    public PersonAdapter(Context context, int resource, List<Person> items) {
        super(context, resource, items);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {

            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.phone_content_line_layout, null);

        }

        Person p = getItem(position);


        if (p != null) {
            //ImageView personImage = (ImageView)v.findViewById(R.id.profil_photo);
            TextView personName = (TextView)v.findViewById(R.id.name_surname);
            TextView personNumber = (TextView)v.findViewById(R.id.phone_number);

           // if(personImage != null && personName != null && personNumber != null){
                if( personName != null && personNumber != null){
                personNumber.setText(p.getNumber());
                personName.setText(p.getName());

            }

        }
        return v;

    }


}
