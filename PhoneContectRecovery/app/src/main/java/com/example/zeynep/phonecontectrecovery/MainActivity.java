package com.example.zeynep.phonecontectrecovery;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    ListView list;
    ArrayList<Person> contactsArrayList;
    PersonAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        contactsArrayList = new ArrayList<Person>();
        list = (ListView) findViewById((R.id.liste));
        getContactInformation();

    }

    //Read the contact from the phone
    public void getContactInformation() {

        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;

        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;
        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(CONTENT_URI, null, null, null, null);

        if (cursor.getCount() > 0) {

            while (cursor.moveToNext()) {


                String contact_id = cursor.getString(cursor.getColumnIndex(_ID));
                String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));
                String phoneNumber = null;

                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER)));

                if (hasPhoneNumber > 0) {

                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[]{contact_id}, null);

                    while (phoneCursor.moveToNext()) {

                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));

                        contactsArrayList.add(new Person(name, phoneNumber,R.drawable.profil));
                    }

                    phoneCursor.close();
                }
            }
        }
        // creating an adapter
        adapter = new PersonAdapter(this, R.layout.phone_content_line_layout, contactsArrayList);
        if (list != null) {
            //attach the adapter to list
            list.setAdapter(adapter);

        }

        // this part to call the number which is clicked.
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> list, View row, int index, long rowID) {
                                            Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + contactsArrayList.get(index).getNumber()));
                                            startActivity(callIntent);
                                        }
                                    }
        );
    }


    public void onClickBackup(View view) throws FileNotFoundException {


        try {
            PrintStream fileOutputStream = new PrintStream(openFileOutput("backup_file", MODE_PRIVATE));
            for (int i = 0; i < contactsArrayList.size(); i++) {
                fileOutputStream.println((contactsArrayList.get(i).getName().toString() + "-" + contactsArrayList.get(i).getNumber().toString() ));
                Toast.makeText(getApplicationContext(),"Back-up is taken!",Toast.LENGTH_LONG).show();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onClickRecovery(View view) {
        try {
            String DispalyName;
            String PhoneNumber;
            Person personObject ;
            String str;

            // To get the file which is in this path
            File file = getApplicationContext().getFileStreamPath("backup_file");
            // it requires to click Back-Up button fisrtly.
            //controling click to Recovery button after clicking Recovery Button
           if(file.exists()) {

                contactsArrayList.clear();
                Scanner scan = new Scanner(openFileInput("backup_file")); // to read from the txt file which is back up file
                while (scan.hasNext()) {

                    // creating a new object Person to seperate every items by reading from the file and assign them to object.
                    personObject= new Person(null, null, 0);
                    String seperatedItems[]=null;
                    seperatedItems = (scan.nextLine().split("-"));
                    personObject.setName(seperatedItems[0].toString());
                    DispalyName = seperatedItems[0].toString();
                    PhoneNumber =seperatedItems[1].toString();
                    personObject.setNumber(seperatedItems[1].toString());
                    // contactsArrayList consists of Person objects
                    contactsArrayList.add(personObject);


                    // to update the phone contacts in the phone
                    ArrayList<ContentProviderOperation> operator = new ArrayList<ContentProviderOperation>();

                    operator.add(ContentProviderOperation.newInsert(
                            ContactsContract.RawContacts.CONTENT_URI)
                            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                            .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                            .build());

                     // to add the person name in the back-up file again to the contact list in the phone.
                    if (DispalyName != null) {
                        operator.add(ContentProviderOperation.newInsert(
                                ContactsContract.Data.CONTENT_URI)
                                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                .withValue(ContactsContract.Data.MIMETYPE,
                                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                                .withValue(
                                        ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                                        DispalyName).build());
                    }

                    // to add the person phone number in the back-up file again to the contact list in the phone.
                    if (PhoneNumber != null) {
                        operator.add(ContentProviderOperation.
                                newInsert(ContactsContract.Data.CONTENT_URI)
                                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                .withValue(ContactsContract.Data.MIMETYPE,
                                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, PhoneNumber)
                                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                                .build());
                    }
                    try {
                        getContentResolver().applyBatch(ContactsContract.AUTHORITY, operator);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

          }
            else
                Toast.makeText(getApplicationContext(), " Firstly take back-up!", Toast.LENGTH_SHORT).show();

            PersonAdapter adapter = new PersonAdapter(this, R.layout.phone_content_line_layout, contactsArrayList);
            list.setAdapter(adapter);





        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }



    }


    //This method filters the numbers according to the operators
    public void filterOperators(View view) {

        Person person = null;
        String phonenumbers = null;
        ArrayList<Person> selected = new ArrayList<Person>();


        boolean checked = ((RadioButton) view).isChecked(); // Button check

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radioButton_Turkcell:
                if (checked)
                    for (int i = 0; i < contactsArrayList.size(); i++) {
                        person = contactsArrayList.get(i);
                        phonenumbers = person.getNumber();
                        String sub = phonenumbers.substring(1, 3);

                        if (sub.contains("53") == true) {
                            selected.add(new Person(person.getName(), phonenumbers,R.drawable.profil));
                        }

                    }
                adapter = new PersonAdapter(this, R.layout.phone_content_line_layout, selected);
                if (list != null) {
                    list.setAdapter(adapter);
                }
                break;

            case R.id.radioButton_TurkTelekom:
                if (checked)
                    for (int i = 0; i < contactsArrayList.size(); i++) {
                        person = contactsArrayList.get(i);
                        phonenumbers = person.getNumber();
                        String sub = phonenumbers.substring(1, 3);

                        if (sub.contains("50") == true || sub.contains("55") == true) {
                            selected.add(new Person(person.getName(), phonenumbers,R.drawable.profil));
                        }

                    }
                adapter = new PersonAdapter(this, R.layout.phone_content_line_layout, selected);
                if (list != null) {
                    list.setAdapter(adapter);
                }
                break;


            case R.id.radioButton_Vodafone:
                if (checked)
                    for (int i = 0; i < contactsArrayList.size(); i++) {
                        person = contactsArrayList.get(i);
                        phonenumbers = person.getNumber();
                        String sub = phonenumbers.substring(1, 3);

                        if (sub.contains("54") == true) {
                            selected.add(new Person(person.getName(), phonenumbers,R.drawable.profil));
                        }

                    }
                adapter = new PersonAdapter(this, R.layout.phone_content_line_layout, selected);
                if (list != null) {
                    list.setAdapter(adapter);
                }
                break;
            case R.id.radioButton_All://If we want to see all operators
                if (checked)
                    for (int i = 0; i < contactsArrayList.size(); i++) {
                        person = contactsArrayList.get(i);
                        phonenumbers = person.getNumber();
                        String sub = phonenumbers.substring(1, 3);
                        selected.add(new Person(person.getName(), phonenumbers,R.drawable.profil));
                    }
                adapter = new PersonAdapter(this, R.layout.phone_content_line_layout, selected);
                if (list != null) {
                    list.setAdapter(adapter);
                }
                break;
        }
        adapter = new PersonAdapter(this, R.layout.phone_content_line_layout, contactsArrayList);
    }
}