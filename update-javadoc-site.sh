set -e
cd `dirname $0`
echo "Generating..."
(cd CommonChickenRuntimeEngine && ant javadoc)
echo "Tarring..."
(cd Javadoc && tar -czf ../javadoc.tgz .)
echo "Uploading..."
scp -r javadoc.tgz cgscomwww.catlin.edu:ccre3-doc.tgz
echo "Updating..."
ssh cgscomwww.catlin.edu 'rm -r ccre3-doc && mkdir ccre3-doc && cd ccre3-doc && tar -xzf ../ccre3-doc.tgz && chmod -R 0755 .'
echo "Done!"
