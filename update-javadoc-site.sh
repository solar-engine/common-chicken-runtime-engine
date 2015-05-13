set -e
cd `dirname $0`
echo "Tarring..."
(cd Javadoc && tar -czf ../javadoc.tgz .)
echo "Uploading..."
scp -r javadoc.tgz cgscomwww.catlin.edu:ccre-doc.tgz
echo "Updating..."
ssh cgscomwww.catlin.edu 'rm -r ccre-doc && mkdir ccre-doc && cd ccre-doc && tar -xzf ../ccre-doc.tgz && chmod -R 0755 .'
echo "Done!"
