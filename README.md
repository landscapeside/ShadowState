[![](https://jitpack.io/v/landscapeside/ShadowState.svg)](https://jitpack.io/#landscapeside/ShadowState)

状态管理器

引入：

```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://www.jitpack.io' }
    }
}
```

```groovy
implementation("com.github.landscapeside.ShadowState:shadowstate:<version>"){
    exclude group: 'org.reactivestreams', module: 'reactive-streams'
} 
kapt 'com.github.landscapeside.ShadowState:shadowstate-compiler:<version>'
```

使用：

* 创建状态类和代理

```kotlin
data class TestState(
    val name: String,
    val amendInternal: Int = 0
)
```

```kotlin
class TestAgent : StateAgent<TestState, MainActivity>() {
    override fun initState(bundle: Bundle?): TestState =
        TestState("hahah")

    override fun conf() {
        listen({ it.name }, {
            view?.setName("$it${state.amendInternal}")
            setState { it.copy(amendInternal = it.amendInternal+1) }
        })
    }
}
```

* 创建共享状态类和代理

```kotlin
data class Share(
  val shareName: String = "",
  val shareCount: Int = 0,
  val item: ShareItem<String> = ShareItem("init")
) {
  data class ShareItem<T>(
    val data: T
  )
}
```

```kotlin
class MainShareAgent : ShadowStateAgent<Share, ShareView>() {

  override fun conf() {
    listen({ it.shareName }, { view?.setShareName(it) })
    listen({ it.shareCount }, { view?.setShareCount(it) })
    listen({ it.item }, { view?.setShareItem(it) })
  }
}
```

* 注册状态类和代理

```kotlin
/*
* 通过BindState注解绑定页面状态
* 可选：通过ShareState共享全局状态
* 
* */

@BindState(state = TestState::class,agent = TestAgent::class)
@ShareState(states = [Share::class],agent = [MainShareAgent::class])
class MainActivity : AppCompatActivity() {
  // 注入代理
  @InjectAgent
  lateinit var agent: TestAgent
  @InjectAgent
  lateinit var shareAgent: MainShareAgent

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    ShadowState.bind(this)
    ShadowState.injectDispatcher(this,this)
    setName(agent.state.name)
  }
  
  override fun onNewIntent(intent:Intent){
    setIntent(intent)
    ShadowState.rebind(this)
    super.onNewIntent(intent)
  }
  
  fun setName(name: String){
    tv_name.text = name
  }

  fun changeName(view: View) {
    agent.setState { it.copy(name = it.name+"++") }
  }
}
```

* 声明状态管理器

```kotlin

// 创建一个空类，并配上StateManagerProvider
@StateManagerProvider
class Main 

```

rebuild后会生成`*StateManager`，在`Application`里面初始化

```kotlin
class TestApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ShadowState.init(
            applicationContext,
            BuildConfig.DEBUG,/*打印日志开关*/
            true,/*启用状态检查器*/
            arrayOf(MainStateManager())
        )
    }
}
```

* 打开检查器

```kotlin
  ShadowState.openWatcher()
```

* 打开共享检查器

```kotlin
  ShadowState.openShareWatcher()
```

* 混淆配置

```
-keep class com.landside.shadowstate.** { *; }
-keep interface com.landside.shadowstate.** { *; }
-keep @com.landside.shadowstate_annotation.BindState class * {*;}
-keep @com.landside.shadowstate_annotation.ShareState class * {*;}
-keep @com.landside.shadowstate_annotation.InjectAgent class * {*;}
-keep @com.landside.shadowstate_annotation.StateManagerProvider class * {*;}
-keep class **.**Binder {*;}
-keep class **.**StateManager {*;}
-keepclassmembers class ** {
    @com.landside.shadowstate_annotation.InjectAgent *;
    <init>();
}

```
