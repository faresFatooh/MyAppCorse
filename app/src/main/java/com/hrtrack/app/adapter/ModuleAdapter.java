package com.hrtrack.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hrtrack.app.R;
import com.hrtrack.app.models.Module;
import com.hrtrack.app.ui.AttendanceActivity;
import com.hrtrack.app.ui.CalendarActivity;
import com.hrtrack.app.ui.EditTaskActivity;
import com.hrtrack.app.ui.ReportsActivity;
import com.hrtrack.app.ui.TaskManagerActivity;

import java.util.List;

public class ModuleAdapter extends RecyclerView.Adapter<ModuleAdapter.ModuleViewHolder> {

    private List<Module> moduleList;
    private Context context;

    public ModuleAdapter(Context context, List<Module> moduleList) {
        this.context = context;
        this.moduleList = moduleList;
    }

    @NonNull
    @Override
    public ModuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_module, parent, false);
        return new ModuleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ModuleViewHolder holder, int position) {
        Module module = moduleList.get(position);
        holder.moduleTitle.setText(module.getTitle());
        holder.moduleIcon.setImageResource(module.getIconRes());

        holder.itemView.setOnClickListener(v -> {
            // عند الضغط، ننتقل إلى النشاط المناسب لتحرير المهام
            // يمكنك استخدام switch-case أو if-else بناءً على العنوان أو أي معرف آخر
            if (module.getTitle().equals("Attendance")) {
                context.startActivity(new Intent(context, AttendanceActivity.class));
            } else if (module.getTitle().equals("Task Manager")) {
                context.startActivity(new Intent(context, TaskManagerActivity.class));
            } else if (module.getTitle().equals("Calendar")) {
                context.startActivity(new Intent(context, CalendarActivity.class));
            } else if (module.getTitle().equals("Reports")) {
                context.startActivity(new Intent(context, ReportsActivity.class));
            } else {
                // مثال على شاشة تحرير عامة
                Intent intent = new Intent(context, EditTaskActivity.class);
                intent.putExtra("moduleTitle", module.getTitle());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return moduleList.size();
    }

    public static class ModuleViewHolder extends RecyclerView.ViewHolder {
        ImageView moduleIcon;
        TextView moduleTitle;
        public ModuleViewHolder(@NonNull View itemView) {
            super(itemView);
            moduleIcon = itemView.findViewById(R.id.module_icon);
            moduleTitle = itemView.findViewById(R.id.module_title);
        }
    }
}
