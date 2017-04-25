require 'octokit'
require 'securerandom'

reportDir = SecureRandom.uuid

githubToken = ENV['GITHUB_TOKEN']

client = Octokit::Client.new( :access_token => githubToken )

output = `git config --get remote.origin.url`
repo = output.strip.sub('git@github.com:', '').strip.sub('https://github.com/', '').sub('.git', '')

ref="heads/gh-pages"

pullMessage = "Reports Dir:\n"

references = repo.split("/");

user = references[0]
repoName = references[1]

Dir.glob('build/reports/**/*').each do|f|
  if not File.directory?(f) then
    puts "Adding File " + f

    file = File.open(f, "r")
    content = file.read
    file.close

    if f.end_with? "index.html" then
        pullMessage = pullMessage + "  + " +  "https://" + user+ ".github.io/" + repoName+ "/" + reportDir + "/" + f + "\n"
    end

    client.create_contents(
        repo, reportDir + "/" + f,
        "Add Test Report " + reportDir + " " + f,
        content,
        :branch => "gh-pages")

    sleep(1000)
 end
end

puts pullMessage

if ENV['TRAVIS_PULL_REQUEST'].eql? ""  then
	client.add_comment(repo, ENV['TRAVIS_PULL_REQUEST'], pullMessage)
end
