for file in $(find -name '*.java')
do
	date=$(git log -n 1 --pretty=format:%ad --date=format:%Y -- $file)
	author=$(git log -n 1 --pretty=format:%an -- $file)
	commit=$(git log -n 1 --pretty=format:%H -- $file)
	headdata=$(head -n 2 -- $file | tail -n 1)
	if echo "$headdata" | grep -q "Copyright" -
	then
		if grep -q "^<$commit $file>$" - <header-dates-ignore.txt
		then
			# don't check
			true
		else
			FAIL="false"
			if echo "$headdata" | grep -q "$author" -
			then
				# found
				true
			else
				echo
				echo "Uncredited author: $headdata"
				FAIL="true"
			fi
			if echo "$headdata" | grep -qE "[-, ]$date " -
			then
				# found
				true
			else
				echo
				echo "Old date: $headdata"
				FAIL="true"
			fi
			while $FAIL
			do
				echo
				echo "$date $author <$commit $file>"
				echo
				read -e -p "(b)lame/ne(w) blame/(a)uthor blame/(e)dit/(i)gnore/e(x)it/(n)ext/(!)...> " cmd
				cmdc="${cmd:0:1}"
				if [ "$cmdc" = "b" ]
				then
					git blame -- $file
				elif [ "$cmdc" = "w" ]
				then
					git blame -- $file | grep "$date"
				elif [ "$cmdc" = "a" ]
				then
					git blame -- $file | grep "$author"
				elif [ "$cmdc" = "e" ]
				then
					nano $file
				elif [ "$cmdc" = "i" ]
				then
					echo "<$commit $file>" >>header-dates-ignore.txt
					echo "There are now $(wc -l header-dates-ignore.txt | cut -d ' ' -f 1) ignored entries."
					FAIL="false"
				elif [ "$cmdc" = "x" ]
				then
					exit 1
				elif [ "$cmdc" = "n" ]
				then
					FAIL="false"
				elif [ "$cmdc" = "!" ]
				then
					${cmd:1}
					echo "Exit code: $?"
				else
					echo "unknown command '$cmdc'"
				fi
			done
		fi
	fi
done
echo "Done"
