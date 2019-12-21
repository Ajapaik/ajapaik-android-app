package ee.ajapaik.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;
import java.util.List;

import ee.ajapaik.android.R;
import ee.ajapaik.android.util.Size;

public class StaggeredGridView extends RecyclerView {
    private int m_cellSpacing = 12;
    private int m_rowHeight = 64;
    private float m_rowHeightMaxDeviation = 0.0F;

    public StaggeredGridView(Context context) {
        super(context);
        readAttributes(context, null);
    }

    public StaggeredGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        readAttributes(context, attrs);
    }

    public StaggeredGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        readAttributes(context, attrs);
    }

    @Override
    public void setAdapter(RecyclerView.Adapter adapter) {
        if(adapter != null) {
            ((Adapter)adapter).setParameters(m_cellSpacing, m_rowHeight, m_rowHeightMaxDeviation);
        }

        super.setAdapter(adapter);
    }

    private void readAttributes(Context context, AttributeSet attrs) {
        if(attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StaggeredGridView);

            m_cellSpacing = a.getDimensionPixelOffset(R.styleable.StaggeredGridView_cellSpacing, m_cellSpacing);
            m_rowHeight = a.getDimensionPixelSize(R.styleable.StaggeredGridView_rowHeight, m_rowHeight);
            m_rowHeightMaxDeviation = a.getFraction(R.styleable.StaggeredGridView_rowHeightMaxDeviation, 1, 0, m_rowHeightMaxDeviation);

            a.recycle();
        }

        setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
    }

    public abstract static class Adapter extends RecyclerView.Adapter<Adapter.DataViewHolder> {
        private static final int VIEW_TYPE_CONTENT = 0;

        private Context m_context;
        private Layout m_layout;
        private int m_cellSpacing = 12;
        private int m_rowHeight = 100;
        private float m_rowHeightMaxDeviation = 0.0F;

        public Adapter(Context context) {
            m_context = context;
        }

        public void setParameters(int cellSpacing, int rowHeight, float rowHeightMaxDeviation) {
            m_cellSpacing = cellSpacing;
            m_rowHeight = rowHeight;
            m_rowHeightMaxDeviation = rowHeightMaxDeviation;

            if(m_layout != null) {
                m_layout = null;
            }
        }

        @Override
        public DataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LinearLayout view = null;

            if(viewType == VIEW_TYPE_CONTENT) {
                view = new LinearLayout(m_context);
                view.setOrientation(LinearLayout.HORIZONTAL);
                view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            }

            return new DataViewHolder(view);
        }

        @Override
        public void onBindViewHolder(DataViewHolder holder, int position) {
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            List<Layout.Item> items = m_layout.getItems(position);
            int cellSpacing = m_layout.getCellSpacing();
            boolean first = true;

            if(position > 0) {
                params.topMargin = cellSpacing;
            }

            holder.layoutView.removeAllViews();
            holder.layoutView.setLayoutParams(params);

            for(Layout.Item item : items) {
                View itemView = createItemView(m_context, holder.layoutView);

                if(itemView != null) {
                    if(!first && cellSpacing > 0) {
                        LinearLayout space = new LinearLayout(m_context);

                        space.setOrientation(LinearLayout.HORIZONTAL);
                        space.setLayoutParams(new LayoutParams(cellSpacing, LayoutParams.WRAP_CONTENT));
                        holder.layoutView.addView(space);
                    } else {
                        first = false;
                    }

                    itemView.setLayoutParams(new LayoutParams(item.width, item.height));
                    holder.layoutView.addView(itemView);
                    bindItemView(item.position, itemView);
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            return VIEW_TYPE_CONTENT;
        }

        @Override
        public int getItemCount() {
            if(m_layout == null) {
                WindowManager wm = (WindowManager)m_context.getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                Point size = new Point();

                display.getSize(size);
                m_layout = new Layout(this, size.x, m_cellSpacing, m_rowHeight, m_rowHeightMaxDeviation);
            }

            return m_layout.getCount();
        }

        public abstract View createItemView(Context context, ViewGroup parent);
        public abstract void bindItemView(int position, View view);
        public abstract Size getSize(int position);
        public abstract int getCount();

        private static class Layout {
            private final int m_width;
            private List<List<Item>> m_items;
            private int m_cellSpacing;

            public Layout(Adapter adapter, int rowWidth, int cellSpacing, int rowHeight, float rowHeightMaxDeviation) {
                List<Integer> sections = new ArrayList<Integer>();
                List<Integer> widths = new ArrayList<Integer>();
                float minScale = 1.0F - rowHeightMaxDeviation;
                float maxScale = 1.0F + rowHeightMaxDeviation;
                int currentWidth = 0;
                int sectionIndex = 0;

                m_cellSpacing = cellSpacing;
                m_width = rowWidth;
                m_items = new ArrayList<List<Item>>();

                for(int i = 0, c = adapter.getCount(); i < c; i++) {
                    Size size = adapter.getSize(i);
                    int width = (int)Math.floor((float)size.width * ((float)rowHeight / (float)size.height));

                    widths.add(width);

                    if(sectionIndex != i && currentWidth + width + cellSpacing < rowWidth) {
                        currentWidth += cellSpacing + width;
                    } else {
                        sectionIndex = i;
                        currentWidth = width;
                    }

                    sections.add(sectionIndex);
                }

                sectionIndex = 0;

                for(int i = 0, c = sections.size(); i < c; i++) {
                    int section = sections.get(i);
                    boolean repeat = false;

                    if(i + 1 == c) {
                        if(section == sectionIndex) {
                            i++;
                            section = -1;
                        } else {
                            repeat = true;
                        }
                    }

                    if(section != sectionIndex) {
                        List<Item> items = new ArrayList<Item>();
                        float scaleX = 1.0F, scaleY = 1.0F;
                        int width = 0;

                        for(int j = sectionIndex; j < i; j++) {
                            width += widths.get(j);
                        }

                        scaleX = (float)(rowWidth - (i - sectionIndex - 1) * cellSpacing) / (float)width;
                        scaleY = scaleX;

                        if(scaleY < minScale) {
                            scaleY = minScale;
                        } else if(scaleY > maxScale) {
                            scaleY = maxScale;
                        }

                        if(i >= c && scaleX > 1.5F * scaleY) {
                            scaleX = scaleY;
                        }

                        for(int j = sectionIndex; j < i; j++) {
                            items.add(new Item(j, Math.round(scaleX * (float)widths.get(j)), Math.round(scaleY * rowHeight)));
                        }

                        m_items.add(items);
                        sectionIndex = section;
                    }

                    if(repeat) {
                        i--;
                    }
                }
            }

            public List<Item> getItems(int position) {
                return m_items.get(position);
            }

            public int getCount() {
                return m_items.size();
            }

            public int getCellSpacing() {
                return m_cellSpacing;
            }

            public int getWidth() {
                return m_width;
            }

            public static class Item {
                public int position;
                public int width;
                public int height;

                public Item(int position, int width, int height) {
                    this.position = position;
                    this.width = width;
                    this.height = height;
                }
            }
        }

        public static class DataViewHolder extends RecyclerView.ViewHolder {
            public final LinearLayout layoutView;

            public DataViewHolder(LinearLayout layoutView) {
                super(layoutView);
                this.layoutView = layoutView;
            }
        }
    }
}
