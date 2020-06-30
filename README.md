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
implementation "com.github.landscapeside.ShadowState:shadowstate:<version>"
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
@BindState(TestState::class)
@BindAgent(TestAgent::class)
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

rebuild后会生成`ShadowStateManager`，在`Application`里面初始化
```kotlin
class TestApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ShadowState.install(
            ShadowStateManager(),
            BuildConfig.DEBUG
        )
    }
}
```