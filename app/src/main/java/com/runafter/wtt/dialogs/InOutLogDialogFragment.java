package com.runafter.wtt.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TimePicker;
import android.widget.Toast;

import com.runafter.wtt.DateTimeUtils;
import com.runafter.wtt.InOutLog;
import com.runafter.wtt.R;

import java.text.ParseException;
import java.util.Calendar;

/**
 * Created by runaf on 2017-01-22.
 */

public class InOutLogDialogFragment extends DialogFragment {
    private static final String TAG = "IOLDF";
    private EditText etDate;
    private EditText etTime;
    private DateSelectListener dateSelectListener;
    private TimeSelectListener timeSelectListener;
    private RadioButton rbTypeIn;
    private RadioButton rbTypeOut;
    private InOutLog base;
    private InOutLogDialogListener listener = new InOutLogDialogListenerAdapter();

    public void setBase(InOutLog log) {
        this.base = log;
    }

    public void setResultListener(InOutLogDialogListener listener) {
        this.listener = listener;
    }

    public interface InOutLogDialogListener {
        void onUpdate(final InOutLog oldInOutLog, final InOutLog newInOutLog);
        void onCreate(final InOutLog inOutLog);
        void onDelete(final InOutLog inOutLog);
    }
    public static class InOutLogDialogListenerAdapter implements InOutLogDialogListener {
        @Override
        public void onUpdate(final InOutLog oldInOutLog, final InOutLog newInOutLog) {
        }

        @Override
        public void onCreate(final InOutLog inOutLog) {
        }

        @Override
        public void onDelete(final InOutLog inOutLog) {
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View content = createContentView();
        builder.setView(content);
        builder.setCancelable(false);
        builder.setMessage(R.string.inoutlog_dialog_title)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // FIRE ZE MISSILES!
                    if (validate()) {
                        InOutLog inOutLog = inOutLogOfDialog();
                        if (updateMode())
                            listener.onUpdate(base, inOutLog);
                        else
                            listener.onCreate(inOutLog);
                    }
                }
            })
            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    getDialog().cancel();
                }
            });
        if (updateMode())
            builder.setNeutralButton(R.string.button_delete, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    listener.onDelete(base);
                }
            });

        // Create the AlertDialog object and return it
        return builder.create();
    }

    private InOutLog inOutLogOfDialog() {
        InOutLog log = new InOutLog();
        try {
            Calendar date = dateOf(etDate);
            Calendar time = timeOf(etTime);
            date.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
            date.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
            log.setTime(date.getTime().getTime());
            log.setType(rbTypeIn.isChecked() ? InOutLog.TYPE_IN : rbTypeOut.isChecked() ? InOutLog.TYPE_OUT : InOutLog.TYPE_UNKNOWN);
            if (base != null)
                log.setDesc(base.getDesc());
            else
                log.setDesc("manual");
            return log;
        } catch (ParseException e) {
            return InOutLog.NULL;
        }
    }

    private boolean validate() {
        if (!isDateFormat(etDate.getText().toString())) {
            showValidationMessage("IN or OUT is required.");
            return false;
        }
        if (!isTimeFormat(etTime.getText().toString())) {
            showValidationMessage("IN or OUT is required.");
            return false;
        }
        if (!this.rbTypeIn.isChecked() && !this.rbTypeOut.isChecked()) {
            showValidationMessage("IN or OUT is required.");
            return false;
        }
        return true;
    }

    private void showValidationMessage(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
    }

    private boolean isDateFormat(String string) {
        if (string == null || string.isEmpty())
            return false;
        try {
            DateTimeUtils.parseDateToCalendar(string);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
    private boolean isTimeFormat(String string) {
        if (string == null || string.isEmpty())
            return false;
        try {
            DateTimeUtils.parseTimeToCalendar(string);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private View createContentView() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View contentView = inflater.inflate(R.layout.dialog_inout_log, null);

        this.etDate = (EditText)contentView.findViewById(R.id.et_date);
        this.etTime = (EditText)contentView.findViewById(R.id.et_time);
        this.rbTypeIn = (RadioButton)contentView.findViewById(R.id.rb_type_in);
        this.rbTypeOut = (RadioButton)contentView.findViewById(R.id.rb_type_out);

        if (updateMode()) {
            Calendar calendar = calendarOf(base);
            this.etDate.setText(formatDate(calendar));
            this.etTime.setText(formatTime(calendar));
            if (InOutLog.TYPE_IN.equals(base.getType()))
                this.rbTypeIn.setChecked(true);
            if (InOutLog.TYPE_OUT.equals(base.getType()))
                this.rbTypeOut.setChecked(true);
        }

        dateSelectListener = new DateSelectListener();
        timeSelectListener = new TimeSelectListener();

        if (!updateMode()) {
            this.etDate.setOnFocusChangeListener(dateSelectListener);
            this.etTime.setOnFocusChangeListener(timeSelectListener);
        }

        this.etDate.setOnClickListener(dateSelectListener);
        this.etTime.setOnClickListener(timeSelectListener);

        return contentView;
    }

    private Calendar calendarOf(InOutLog base) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(base.getTime());
        return calendar;
    }

    private boolean updateMode() {
        return base != null;
    }

    protected class DateSelectListener implements View.OnFocusChangeListener, View.OnClickListener {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus)
                showDatePickerDialogOn((EditText) v);
        }

        @Override
        public void onClick(View v) {
            showDatePickerDialogOn((EditText) v);
        }
    }
    protected class TimeSelectListener implements View.OnFocusChangeListener, View.OnClickListener {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus)
                showTimePickerDialogOn((EditText) v);
        }

        @Override
        public void onClick(View v) {
            showTimePickerDialogOn((EditText) v);
        }
    }

    private Calendar dateOf(EditText view) throws ParseException {
        String string = view.getText().toString().toString();
        if (string.isEmpty())
            return null;
        return DateTimeUtils.parseDateToCalendar(string);
    }

    private void showDatePickerDialogOn(final EditText editText) {
        try {
            Calendar calendar = dateOf(editText);
            calendar = calendar == null ? today() : calendar;
            new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    editText.setText(formatDate(calendarOf(year, month, dayOfMonth)));
                    nextElementOf(editText).requestFocus();
                }
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        } catch (ParseException e) {
            Toast.makeText(getActivity(), "Invalid date format.", Toast.LENGTH_SHORT);
            Log.e(TAG, "Invalid date format. " + editText.getText().toString(), e);
        }
    }

    private void showTimePickerDialogOn(final EditText editText) {
        try {
            Calendar calendar = timeOf(editText);
            calendar = calendar == null ? today() : calendar;
            new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {

                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    editText.setText(formatTime(calendarOf(hourOfDay, minute)));
                    nextElementOf(editText).requestFocus();
                }

            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        } catch (ParseException e) {
            Toast.makeText(getActivity(), "Invalid time format.", Toast.LENGTH_SHORT);
            Log.e(TAG, "Invalid time format. " + editText.getText().toString(), e);
        }
    }


    private String formatTime(Calendar calendar) {
        return DateTimeUtils.formatTime(calendar);
    }

    private Calendar timeOf(EditText view) throws ParseException {
        String string = view.getText().toString().toString();
        if (string.isEmpty())
            return null;
        return DateTimeUtils.parseTimeToCalendar(string);
    }

    private View nextElementOf(View view) {
        if (view == etDate)
            return etTime;
        if (view == etTime)
            return rbTypeIn;
        return getView();
    }

    private Calendar today() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    private String formatDate(Calendar calendar) {
        return DateTimeUtils.formatDate(calendar);
    }

    private Calendar calendarOf(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        return DateTimeUtils.minimumInDate(calendar);
    }


    private Calendar calendarOf(int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }
}
