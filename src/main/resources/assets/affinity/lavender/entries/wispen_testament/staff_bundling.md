```json
{
  "title": "权杖捆绑",
  "icon": "affinity:nimble_staff{bundled_staffs:[{id:\"affinity:kinesis_staff\", Count: 1b}, {id:\"affinity:cultivation_staff\", Count: 1b}, {id:\"affinity:collection_staff\", Count: 1b}]}",
  "category": "affinity:equipment",
  "required_advancements": [
    "affinity:big_girl_decisions"
  ]
}
```

在合成了许多{concept}权杖{}之后，因背包管理问题而头疼的情况并不少见。


不过，很方便的是，只需在{concept}背包中拿着权杖右键点击另一个权杖{}，就能将它们捆绑在一起（只占用一个背包槽）。


由此便可合成{item}权杖束{}。

;;;;;

```xml owo-ui
<stack-layout>
    <children>
        <texture texture="affinity:textures/gui/wispen_testament.png" texture-width="512" texture-height="256"
                 u="381" v="186" region-width="70" region-height="70">
            <blend>true</blend>
        </texture>

        <item>
            <stack>affinity:nimble_staff{bundled_staffs:[{id:"affinity:kinesis_staff", Count: 1b}, {id:"affinity:cultivation_staff", Count: 1b}, {id:"affinity:collection_staff", Count: 1b}]}</stack>
            <set-tooltip-from-stack>true</set-tooltip-from-stack>

            <sizing>
                <horizontal method="fixed">64</horizontal>
                <vertical method="fixed">64</vertical>
            </sizing>
        </item>
    </children>

    <vertical-alignment>center</vertical-alignment>
    <horizontal-alignment>center</horizontal-alignment>

    <sizing>
    <horizontal method="fill">100</horizontal>
    </sizing>
</stack-layout>
```

其功能与中央权杖的功能完全相同。{concept}可以在任何时候{}通过手持权杖束的同时按下<keybind;key.affinity.select_staff_from_bundle>键来{concept}交换中央权杖{}。

@next-page



此外，所有权杖都可以通过在背包中{concept}右键点击{}权杖束{concept}逐个从捆绑中分离出来{}。

@entry-end