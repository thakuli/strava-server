(ns strava-server.testi
  (:require [clojure.data.json :as json]))

(def coords '([61.49487 23.80285][61.49402 23.80181][61.49319 23.80225][61.49109 23.81012][61.49071 23.81008][61.48875 23.81973][61.48698 23.8235][61.48496 23.8245][61.48441 23.82581][61.48336 23.82673][61.48119 23.82782][61.47684 23.82694][61.47473 23.82522][61.47497 23.82523][61.47499 23.82697][61.47391 23.83223][61.47376 23.83599][61.47253 23.841][61.47239 23.84462][61.47153 23.8474][61.47189 23.84919][61.47158 23.8513][61.46922 23.85649][61.46933 23.86127][61.46795 23.86388][61.46577 23.86488][61.46653 23.86464][61.46753 23.86277][61.46804 23.86378][61.46892 23.86258][61.46942 23.86087][61.46932 23.85699][61.46964 23.85726][61.4704 23.86094][61.47028 23.8618][61.46956 23.86314][61.46837 23.86404][61.4681 23.86572][61.46609 23.86518][61.46754 23.86274][61.46808 23.86376][61.46896 23.86248][61.46938 23.86113][61.46939 23.85718][61.47037 23.86162][61.46952 23.86315][61.46838 23.86394][61.46808 23.86573][61.46607 23.86512][61.46755 23.86274][61.46806 23.86376][61.46898 23.86243][61.46938 23.86114][61.46941 23.85717][61.47036 23.86161][61.4695 23.86316][61.4684 23.86391][61.46811 23.86567][61.46606 23.86504][61.468 23.86384][61.4689 23.86257][61.46987 23.86555][61.46997 23.87065][61.47035 23.87151][61.46967 23.8756][61.47056 23.88009][61.47033 23.88137][61.4693 23.88263][61.46915 23.88819][61.46834 23.88942][61.46846 23.89482][61.46746 23.89761][61.46738 23.90201][61.46769 23.90417][61.46741 23.90719][61.4681 23.91032][61.46709 23.91559][61.46628 23.91732][61.46531 23.9224][61.46469 23.92368][61.46057 23.92164][61.46034 23.92421][61.45985 23.92532][61.46129 23.94661][61.46229 23.95498][61.46333 23.95961][61.46385 23.96085][61.46676 23.95907][61.46864 23.95948][61.46769 23.95917][61.46754 23.95572][61.46925 23.95214][61.46934 23.94274][61.47047 23.9419][61.47034 23.93716][61.47097 23.93625][61.47243 23.93577][61.47397 23.93147][61.47373 23.92379][61.47425 23.91819][61.47501 23.91773][61.47715 23.91886][61.48126 23.9225][61.48293 23.90702][61.48321 23.90666][61.48655 23.88318][61.48887 23.87264][61.48907 23.85813][61.48935 23.85744][61.49031 23.85715][61.49052 23.85607][61.49114 23.8563][61.49227 23.85346][61.49821 23.82349][61.49809 23.82095][61.49922 23.81959][61.50072 23.81073][61.49963 23.80679][61.49801 23.8052][61.49697 23.8033]))



;;(def coords '( [10 2] [11 2] [12 3] [15 5] [20 7] [11 3] [30 15]))


(defn calculate-diff [coord1 coord2]
  (let [diff 0.004
        [x1 y1] coord1
        [x2 y2] coord2
        difx (Math/abs (- x1 x2))
        dify (Math/abs (- y1 y2))]
    (if (and ( < difx diff) (< dify diff))
      true
      false)))

(defn num-of-occurences [coord coords]
  (reduce (fn [acc my-coord]
            (if (calculate-diff my-coord coord)
              (inc acc)
              acc)) 0 coords))

(defn reduce-coords-list [coords]
  (for [ coord coords ]
    {:lat (first coord) :lng (first (rest coord)) :count (num-of-occurences coord coords)}))

(defn matching-coords [coords]
  (json/write-str (take 10 (reduce-coords-list coords))))