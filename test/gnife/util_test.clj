(ns gnife.util-test
  (:require [clojure.test :as test :refer [are deftest is]]
            [gnife.util :as util]))

(deftest corename-test
  (are [file expected] (= (util/corename file) expected)
    "foobar.txt" "foobar"
    "foo/bar/foobar.txt" "foobar"
    "foo/bar/foobar.txt.gz" "foobar.txt"
    "foo/bar/foobar" "foobar"
    "foo/bar/.foobar.txt" ".foobar"
    "foo/bar/.foobar" ".foobar")
  (is (thrown? Exception (util/corename nil))))

(deftest corepath-test
  (are [file expected] (= (util/corepath file) expected)
    "foobar.txt" "foobar"
    "foo/bar/foobar.txt" "foo/bar/foobar"
    "foo/bar/foobar.txt.gz" "foo/bar/foobar.txt"
    "foo/bar/foobar" "foo/bar/foobar"
    "foo/bar/.foobar.txt" "foo/bar/.foobar"
    "foo/bar/.foobar" "foo/bar/.foobar")
  (is (thrown? Exception (util/corepath nil))))
