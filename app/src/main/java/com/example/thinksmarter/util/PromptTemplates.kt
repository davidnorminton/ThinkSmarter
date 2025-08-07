package com.example.thinksmarter.util

object PromptTemplates {
    
    fun generateQuestionPrompt(difficulty: Int, category: String = "General", lengthPreference: String = "Auto"): String {
        val expectedLength = when (lengthPreference) {
            "Short" -> "Short (1-3 sentences)"
            "Medium" -> "Medium (4-6 sentences)"
            "Long" -> "Long (7+ sentences)"
            else -> when {
                difficulty <= 3 -> "Short (1-3 sentences)"
                difficulty <= 7 -> "Medium (4-6 sentences)"
                else -> "Long (7+ sentences)"
            }
        }
        
        return """
            You are a critical thinking coach. The user is practicing how to think clearly and communicate responses effectively.
            
            Generate one thought-provoking question at difficulty level $difficulty (where 1 is very basic and 10 is extremely complex).
            
            Category: $category
            Expected Answer Length: $expectedLength
            
            Requirements:
            - Label the difficulty level clearly
            - Focus the question on reasoning, trade-offs, or clear explanation
            - Make it engaging and thought-provoking
            - Avoid yes/no questions
            - Encourage deep thinking and analysis
            - Ensure the question is relevant to the $category category
            - Design the question so it can be answered appropriately in $expectedLength
            - For short answers: Focus on key points and concise reasoning
            - For medium answers: Allow for some elaboration and examples
            - For long answers: Encourage comprehensive analysis with multiple perspectives
            
            Format your response as just the question text, nothing else.
        """.trimIndent()
    }
    
    fun evaluateAnswerPrompt(question: String, userAnswer: String, expectedLength: String): String {
        val lengthGuidance = when (expectedLength) {
            "Short" -> "Keep feedback concise and focused on key improvements."
            "Medium" -> "Provide balanced feedback covering multiple aspects."
            "Long" -> "Offer comprehensive feedback with detailed suggestions."
            else -> "Provide appropriate feedback for the answer length."
        }
        
        return """
            You are an expert critical thinking evaluator. Analyze the following answer to a critical thinking question.

            QUESTION: $question
            USER'S ANSWER: $userAnswer
            Expected Length: $expectedLength
            $lengthGuidance

            Please provide a comprehensive evaluation in the following format:

            CLARITY SCORE: [1-10]
            [Brief explanation of clarity score]

            LOGIC SCORE: [1-10]
            [Brief explanation of logic score]

            PERSPECTIVE SCORE: [1-10]
            [Brief explanation of perspective score]

            DEPTH SCORE: [1-10]
            [Brief explanation of depth score]

            FEEDBACK:
            [Provide constructive feedback focusing on how to improve clarity, logic, perspective, and depth. Include specific suggestions for length appropriateness.]

            WORD AND PHRASE SUGGESTIONS:
            [Suggest 3-5 better words or phrases that could replace weaker language in the user's answer. Focus on more precise, academic, or impactful alternatives. For example: "Instead of 'I think' use 'I believe' or 'Based on the evidence'", "Replace 'good' with 'effective' or 'beneficial'", etc.]

            BETTER ANSWER SUGGESTIONS:
            [Provide 2-3 specific suggestions for how the user could improve their answer. Include concrete examples of better phrasing, structure, or content. For example: "Consider starting with a clear thesis statement", "Add a counterargument to strengthen your position", "Use more specific examples to support your points", etc.]

            THOUGHT PROCESS GUIDANCE:
            [Provide 2-3 specific thought processes, frameworks, or approaches that would help the user tackle similar problems in the future. Include step-by-step methods, analytical frameworks, or critical thinking strategies.]

            MODEL ANSWER:
            [Provide a well-structured model answer that demonstrates excellent clarity, logic, perspective, and depth within the expected length constraints. This should serve as an example of what an excellent answer would look like.]
        """.trimIndent()
    }
    
    fun getLengthDescription(difficulty: Int): String {
        return when {
            difficulty <= 3 -> "Short (1-3 sentences) - Focus on key points and concise reasoning"
            difficulty <= 7 -> "Medium (4-6 sentences) - Allow for some elaboration and examples"
            else -> "Long (7+ sentences) - Encourage comprehensive analysis with multiple perspectives"
        }
    }
    
    fun generateFollowUpQuestionsPrompt(originalQuestion: String, userAnswer: String): String {
        return """
            Based on the original question and the user's answer, generate 3 follow-up questions that would help deepen their understanding and critical thinking.
            
            Original Question: $originalQuestion
            User's Answer: $userAnswer
            
            Generate 3 follow-up questions that:
            1. Challenge assumptions made in the answer
            2. Explore alternative perspectives
            3. Apply the reasoning to a different context
            
            Format your response as:
            1. [Question text] (Score: [difficulty 1-10])
            2. [Question text] (Score: [difficulty 1-10])
            3. [Question text] (Score: [difficulty 1-10])
            
            Make the questions progressively more challenging and thought-provoking.
        """.trimIndent()
    }
    
    fun improveTextPrompt(userText: String, textType: String = "General"): String {
        return """
            You are an expert writing coach. Analyze the following text and provide comprehensive feedback for improvement.
            
            TEXT TYPE: $textType
            USER'S TEXT: $userText
            
            CRITICAL: You MUST provide ALL 9 sections below in EXACTLY this format. Do not skip any sections. Each section must start with the exact header shown.
            
            CLARITY SCORE: [1-10]
            [Brief explanation of clarity score - how well ideas are communicated]
            
            LOGIC SCORE: [1-10]
            [Brief explanation of logic score - logical flow and reasoning]
            
            PERSPECTIVE SCORE: [1-10]
            [Brief explanation of perspective score - breadth of viewpoints]
            
            DEPTH SCORE: [1-10]
            [Brief explanation of depth score - thoroughness and insight]
            
            FEEDBACK:
            [Provide specific feedback on structure, clarity, and overall effectiveness. Include suggestions for better organization and expression. Be detailed and constructive.]
            
            WORD AND PHRASE SUGGESTIONS:
            [Suggest 5-7 better words or phrases that could replace weaker language. Focus on more precise, impactful alternatives. Format as: "Instead of X, use Y" or "Replace X with Y".]
            
            BETTER ANSWER SUGGESTIONS:
            [Provide 2-3 specific suggestions for how to improve the text. Include concrete examples of better phrasing, structure, or content. Be specific and actionable.]
            
            THOUGHT PROCESS GUIDANCE:
            [Provide 2-3 suggestions for how the user could improve their writing process and approach similar texts in the future. Include step-by-step methods or frameworks.]
            
            MODEL ANSWER:
            [Provide a comprehensive, well-structured version of the text that serves as an excellent example. This should be a complete rewrite that demonstrates best practices.]
        """.trimIndent()
    }
    
    fun generateRandomFactPrompt(category: String): String {
        return """
            You are an expert educator and knowledge curator. Generate an interesting, educational random fact from the $category category.
            
            Requirements:
            - Make it genuinely interesting and surprising
            - Keep it concise (2-3 sentences maximum)
            - Ensure it's accurate and educational
            - Make it engaging and memorable
            - Focus on lesser-known facts rather than common knowledge
            - Include a brief explanation of why it's interesting
            - Make it relevant to the $category category
            
            Format your response as just the fact with its brief explanation, nothing else.
            
            Example format:
            "The human brain can process images in as little as 13 milliseconds - faster than the blink of an eye. This incredible speed allows us to quickly identify threats and opportunities in our environment."
        """.trimIndent()
    }
    
    fun generateMetacognitiveGuidancePrompt(userInput: String): String {
        return """
            You are an expert in metacognition and cognitive psychology. The user has described a task or problem they need to solve. Provide them with metacognitive strategies and thinking techniques to help them approach their problem more effectively.

            User's description: $userInput

            Provide a comprehensive response that includes:

            1. **Problem Analysis**: Help them break down their problem into manageable components
            2. **Metacognitive Strategies**: Suggest specific techniques like:
               - Self-questioning strategies
               - Planning and monitoring approaches
               - Reflection techniques
               - Cognitive load management
               - Mental models and frameworks
            3. **Thinking Frameworks**: Recommend specific frameworks like:
               - First principles thinking
               - Systems thinking
               - Design thinking
               - Critical thinking steps
            4. **Practical Steps**: Provide actionable next steps they can take
            5. **Self-Monitoring**: Suggest ways to track their progress and adjust their approach

            Format your response in a clear, structured manner with headings and bullet points. Be encouraging and supportive while providing practical, actionable advice. Keep it educational and empowering.
        """.trimIndent()
    }
}