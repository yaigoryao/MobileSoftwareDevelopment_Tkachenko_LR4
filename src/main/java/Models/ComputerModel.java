package Models;
import android.os.Parcel;
import android.os.Parcelable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class ComputerModel implements Parcelable {
    public static final int INVALID_ID = -123;

    public enum MotherboardManufacturer {
        ASUS("ASUS"),
        MSI("MSI"),
        Gigabyte("Gigabyte"),
        ASRock("ASRock"),
        Intel("Intel"),
        Biostar("Biostar"),
        EVGA("EVGA"),
        NO_MANUFACTURER("");

        private String _name;

        MotherboardManufacturer(String name) {
            _name = name;
        }

        public static MotherboardManufacturer fromString(String string)
        {
            for (MotherboardManufacturer m : MotherboardManufacturer.values())
                if (m.toString().equalsIgnoreCase(string)) return m;
            return NO_MANUFACTURER;
        }

        @Override
        public String toString() { return this._name; }
    }

    public enum VideocardManufacturer {
        NVIDIA("NVIDIA"),
        AMD("AMD"),
        ASUS("ASUS"),
        MSI("MSI"),
        Gigabyte("Gigabyte"),
        EVGA("EVGA"),
        Zotac("Zotac"),
        NO_MANUFACTURER("");

        private String _name;

        VideocardManufacturer(String name) {
            _name = name;
        }

        public static VideocardManufacturer fromString(String string) {
            for (VideocardManufacturer m : VideocardManufacturer.values())
                if (m.toString().equalsIgnoreCase(string)) return m;

            return NO_MANUFACTURER;
        }

        @Override
        public String toString() { return this._name; }
    }

    public enum MonitordManufacturer {
        Samsung("Samsung"),
        LG("LG"),
        Dell("Dell"),
        ASUS("ASUS"),
        Acer("Acer"),
        BenQ("BenQ"),
        ViewSonic("ViewSonic"),
        NO_MANUFACTURER("");

        private String _name;

        MonitordManufacturer(String name) {
            _name = name;
        }

        public static MonitordManufacturer fromString(String string) {
            for (MonitordManufacturer m : MonitordManufacturer.values())
                if (m.toString().equalsIgnoreCase(string)) return m;

            return NO_MANUFACTURER;
        }

        @Override
        public String toString() { return this._name; }
    }

    private int _id = INVALID_ID;
    public MotherboardManufacturer motherboardManufacturer;
    public VideocardManufacturer videocardManufacturer;
    private int _ram = 1;
    public LocalDate manufactureDate = LocalDate.now();
    public MonitordManufacturer monitordManufacturer;

    public ComputerModel() {}

    public ComputerModel(int id, MotherboardManufacturer motherboardManufacturer, VideocardManufacturer videocardManufacturer, int ram, LocalDate manufactureDate, MonitordManufacturer monitordManufacturer) {
        this.setId(id);
        this.motherboardManufacturer = motherboardManufacturer;
        this.videocardManufacturer = videocardManufacturer;
        this.setRam(ram);
        this.manufactureDate = manufactureDate;
        this.monitordManufacturer = monitordManufacturer;
    }

    public ComputerModel(int id, String motherboardManufacturer, String videocardManufacturer, int ram, String manufactureDate, String monitorManufacturer, boolean unixTime) {
        this(id, MotherboardManufacturer.fromString(motherboardManufacturer), VideocardManufacturer.fromString(videocardManufacturer),
                ram, unixTime ? Instant.ofEpochMilli(Long.parseLong(manufactureDate)).atZone(ZoneId.systemDefault()).toLocalDate() : LocalDate.parse(manufactureDate),
                MonitordManufacturer.fromString(monitorManufacturer));
    }

    public int getRam() { return _ram; }

    public void setRam(int ram) { if (ram > 1) _ram = ram; }

    public int getId() { return _id; }

    public void setId(int id) { if (id > -1) _id = id; }

    protected ComputerModel(Parcel in) {
        setId(in.readInt());
        motherboardManufacturer = MotherboardManufacturer.fromString(in.readString());
        videocardManufacturer = VideocardManufacturer.fromString(in.readString());
        setRam(in.readInt());
        manufactureDate = LocalDate.parse(in.readString());
        monitordManufacturer = MonitordManufacturer.fromString(in.readString());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(_id);
        dest.writeString(motherboardManufacturer.toString());
        dest.writeString(videocardManufacturer.toString());
        dest.writeInt(_ram);
        dest.writeString(manufactureDate.toString());
        dest.writeString(monitordManufacturer.toString());
    }

    @Override
    public int describeContents() { return 0; }

    public static final Creator<ComputerModel> CREATOR = new Creator<ComputerModel>()
    {
        @Override
        public ComputerModel createFromParcel(Parcel in) { return new ComputerModel(in); }

        @Override
        public ComputerModel[] newArray(int size) { return new ComputerModel[size]; }
    };

    @Override
    public String toString()
    {
        return "ID: " + _id + " Производитель мат. платы: " + monitordManufacturer.toString() +
                " Производитель видеокарты: " + videocardManufacturer.toString() +
                " Объем ОП: " + _ram + " Дата изготовления: " + manufactureDate +
                " Производитель монитора: " + monitordManufacturer.toString();
    }
}