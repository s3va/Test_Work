package tk.kvakva.testwork

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController


private const val REQUEST_CODE_PERMISSIONS = 10
private val REQUIRED_PERMISSIONS = arrayOf(
    Manifest.permission.READ_CONTACTS,
    Manifest.permission.READ_EXTERNAL_STORAGE
)


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        setupActionBarWithNavController(findNavController(R.id.fragment))
    }

    /**
     * Check if all permission specified in the manifest have been granted
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * This method is called whenever the user chooses to navigate Up within your application's
     * activity hierarchy from the action bar.
     *
     *
     * If a parent was specified in the manifest for this activity or an activity-alias to it,
     * default Up navigation will be handled automatically. See
     * [.getSupportParentActivityIntent] for how to specify the parent. If any activity
     * along the parent chain requires extra Intent arguments, the Activity subclass
     * should override the method [.onPrepareSupportNavigateUpTaskStack]
     * to supply those arguments.
     *
     *
     * See [Tasks and
 * Back Stack]({@docRoot}guide/topics/fundamentals/tasks-and-back-stack.html) from the developer guide and
     * [Navigation]({@docRoot}design/patterns/navigation.html) from the design guide
     * for more information about navigating within your app.
     *
     *
     * See the [androidx.core.app.TaskStackBuilder] class and the Activity methods
     * [.getSupportParentActivityIntent], [.supportShouldUpRecreateTask], and
     * [.supportNavigateUpTo] for help implementing custom Up navigation.
     *
     * @return true if Up navigation completed successfully and this Activity was finished,
     * false otherwise.
     */
    override fun onSupportNavigateUp(): Boolean {
        findNavController(R.id.fragment).navigateUp()
        return super.onSupportNavigateUp()
    }
}