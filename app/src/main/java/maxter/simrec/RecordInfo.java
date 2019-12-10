package maxter.simrec;

import android.os.Parcel;
import android.os.Parcelable;

public class RecordInfo implements Parcelable {
    public int mId;
    public String mName;
    public String mPath;
    public long mLength;
    public long mCreatedTime;

    public RecordInfo(int id, String name, String path, long length, long createdTime) {
        mId = id;
        mName = name;
        mPath = path;
        mLength = length;
        mCreatedTime = createdTime;
    }

    public RecordInfo() {

    }

    protected RecordInfo(Parcel in) {
        mId = in.readInt();
        mName = in.readString();
        mPath = in.readString();
        mLength = in.readInt();
        mCreatedTime = in.readLong();
    }

    public static final Creator<RecordInfo> CREATOR = new Creator<RecordInfo>() {
        @Override
        public RecordInfo createFromParcel(Parcel in) {
            return new RecordInfo(in);
        }

        @Override
        public RecordInfo[] newArray(int size) {
            return new RecordInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(mName);
        dest.writeString(mPath);
        dest.writeLong(mLength);
        dest.writeLong(mCreatedTime);
    }
}
