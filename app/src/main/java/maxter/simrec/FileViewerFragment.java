package maxter.simrec;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;


public class FileViewerFragment extends Fragment {

    RecyclerView mRecyclerView = null;
    FileViewerAdapter mAdapter = null;

    public FileViewerFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View outputView =  inflater.inflate(R.layout.fragment_file_viewer, container, false);
        mRecyclerView = outputView.findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);

        mAdapter = new FileViewerAdapter(getActivity(), layoutManager);
        mRecyclerView.setAdapter(mAdapter);

        SwipeToDeleteCallback swipeToDelete = new SwipeToDeleteCallback(getActivity(), mAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(swipeToDelete);
        touchHelper.attachToRecyclerView(mRecyclerView);

        return outputView;
    }

}
