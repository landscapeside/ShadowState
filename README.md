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

* 注册状态类和代理

```kotlin
@BindState(state = TestState::class,agent = TestAgent::class)
class MainActivity : AppCompatActivity() {
  // 注入代理
  @InjectAgent
  lateinit var agent: TestAgent

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    ShadowState.bind(this)
    ShadowState.injectDispatcher(this)
    setName(agent.state.name)
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
        ZipStateManager.zip(MainStateManager())
        ShadowState.init(
            applicationContext,
            BuildConfig.DEBUG,
            true
        )
    }
}
```

* 打开检查器

```kotlin
  ShadowState.openWatcher()
```

* 混淆配置

```
-keep class com.landside.shadowstate.** { *; }
-keep interface com.landside.shadowstate.** { *; }
-keep @com.landside.shadowstate_annotation.BindState class * {*;}
-keep @com.landside.shadowstate_annotation.InjectAgent class * {*;}
-keep @com.landside.shadowstate_annotation.StateManagerProvider class * {*;}
-keep class **.**Binder {*;}
-keep class **.**StateManager {*;}
-keepclassmembers class ** {
    @com.landside.shadowstate_annotation.InjectAgent *;
    <init>();
}

```
